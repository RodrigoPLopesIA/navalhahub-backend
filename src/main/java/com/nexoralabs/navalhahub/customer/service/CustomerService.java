package com.nexoralabs.navalhahub.customer.service;

import com.nexoralabs.navalhahub.barbershop.entity.Barbershop;
import com.nexoralabs.navalhahub.barbershop.entity.BarbershopCustomer;
import com.nexoralabs.navalhahub.barbershop.entity.embeddables.PhoneNumber;
import com.nexoralabs.navalhahub.barbershop.repository.BarbershopCustomerRepository;
import com.nexoralabs.navalhahub.barbershop.repository.BarbershopMemberRepository;
import com.nexoralabs.navalhahub.barbershop.repository.BarbershopRepository;
import com.nexoralabs.navalhahub.customer.dto.request.CreateCustomerRequest;
import com.nexoralabs.navalhahub.customer.dto.request.UpdateCustomerRequest;
import com.nexoralabs.navalhahub.customer.dto.response.CustomerResponse;
import com.nexoralabs.navalhahub.customer.entity.Customer;
import com.nexoralabs.navalhahub.customer.mapper.CustomerMapper;
import com.nexoralabs.navalhahub.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final BarbershopCustomerRepository barbershopCustomerRepository;
    private final BarbershopRepository barbershopRepository;
    private final BarbershopMemberRepository barbershopMemberRepository;
    private final CustomerMapper mapper;

    /**
     * Cadastra o cliente na barbearia. O Customer é global (único por telefone):
     * se já existir, reaproveita e apenas cria o vínculo. O cadastro global nunca é sobrescrito.
     */
    @Transactional
    public CustomerResponse create(String identityId, UUID barbershopId, CreateCustomerRequest request) {
        assertMember(identityId, barbershopId);
        Barbershop barbershop = getBarbershop(barbershopId);

        PhoneNumber phone = mapper.toEntity(request.phoneNumber());

        Customer customer = customerRepository
                .findByPhoneNumberCountryCodeAndPhoneNumberNumber(phone.getCountryCode(), phone.getNumber())
                .orElseGet(() -> customerRepository.save(buildCustomer(request, phone)));

        if (!customer.isActive()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este cliente não pode ser cadastrado");
        }
        if (barbershopCustomerRepository.existsByBarbershopIdAndCustomerId(barbershopId, customer.getId())) {
            throw alreadyRegistered();
        }

        BarbershopCustomer link = BarbershopCustomer.builder()
                .barbershop(barbershop)
                .customer(customer)
                .name(resolveDisplayName(request))
                .notes(blankToNull(request.notes()))
                .blocked(false)
                .consentAt(request.consent() ? Instant.now() : null)
                .build();

        try {
            link = barbershopCustomerRepository.saveAndFlush(link);
        } catch (DataIntegrityViolationException e) {
            // requisição concorrente: a unique (barbershop_id, customer_id) venceu
            throw alreadyRegistered();
        }
        return mapper.toResponse(link);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> list(String identityId, UUID barbershopId,
                                       String search, Boolean blocked, Pageable pageable) {
        assertMember(identityId, barbershopId);

        boolean hasSearch = search != null && !search.isBlank();
        Page<BarbershopCustomer> page;

        if (hasSearch) {
            String term = escapeLike(search.trim().toLowerCase(Locale.ROOT));
            page = blocked == null
                    ? barbershopCustomerRepository.search(barbershopId, term, pageable)
                    : barbershopCustomerRepository.searchByBlocked(barbershopId, term, blocked, pageable);
        } else {
            page = blocked == null
                    ? barbershopCustomerRepository.findByBarbershopId(barbershopId, pageable)
                    : barbershopCustomerRepository.findByBarbershopIdAndBlocked(barbershopId, blocked, pageable);
        }
        return page.map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(String identityId, UUID barbershopId, UUID customerId) {
        assertMember(identityId, barbershopId);
        return mapper.toResponse(getLink(barbershopId, customerId));
    }

    @Transactional
    public CustomerResponse update(String identityId, UUID barbershopId, UUID customerId,
                                   UpdateCustomerRequest request) {
        assertMember(identityId, barbershopId);
        BarbershopCustomer link = getLink(barbershopId, customerId);

        if (request.name() != null && !request.name().isBlank()) {
            link.setName(request.name().trim());
        }
        if (request.notes() != null) {
            link.setNotes(blankToNull(request.notes()));
        }
        return mapper.toResponse(barbershopCustomerRepository.save(link));
    }

    @Transactional
    public CustomerResponse block(String identityId, UUID barbershopId, UUID customerId) {
        return setBlocked(identityId, barbershopId, customerId, true);
    }

    @Transactional
    public CustomerResponse unblock(String identityId, UUID barbershopId, UUID customerId) {
        return setBlocked(identityId, barbershopId, customerId, false);
    }

    /** Remove só o vínculo com a barbearia. O Customer global é preservado. */
    @Transactional
    public void remove(String identityId, UUID barbershopId, UUID customerId) {
        assertMember(identityId, barbershopId);
        barbershopCustomerRepository.delete(getLink(barbershopId, customerId));
    }

    // ---------------- helpers ----------------

    private CustomerResponse setBlocked(String identityId, UUID barbershopId, UUID customerId, boolean blocked) {
        assertMember(identityId, barbershopId);
        BarbershopCustomer link = getLink(barbershopId, customerId);
        link.setBlocked(blocked);
        return mapper.toResponse(barbershopCustomerRepository.save(link));
    }

    /** Autorização multi-tenant: o usuário precisa pertencer à barbearia. Sempre roda antes de qualquer leitura. */
    private void assertMember(String identityId, UUID barbershopId) {
        if (!barbershopMemberRepository.existsByBarbershopIdAndUser_IdentityId(barbershopId, identityId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem acesso a esta barbearia");
        }
    }

    private Barbershop getBarbershop(UUID barbershopId) {
        return barbershopRepository.findById(barbershopId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Barbearia não encontrada"));
    }

    private BarbershopCustomer getLink(UUID barbershopId, UUID customerId) {
        return barbershopCustomerRepository.findByBarbershopIdAndCustomerId(barbershopId, customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Cliente não encontrado nesta barbearia"));
    }

    private Customer buildCustomer(CreateCustomerRequest request, PhoneNumber phone) {
        return Customer.builder()
                .id(UUID.randomUUID())
                .firstName(request.firstName().trim())
                .lastName(blankToNull(request.lastName()))
                .email(request.email() == null || request.email().isBlank()
                        ? null : request.email().trim().toLowerCase(Locale.ROOT))
                .phoneNumber(phone)
                .active(true)
                .build();
    }

    private String resolveDisplayName(CreateCustomerRequest request) {
        if (request.name() != null && !request.name().isBlank()) {
            return request.name().trim();
        }
        String last = request.lastName() == null || request.lastName().isBlank()
                ? "" : " " + request.lastName().trim();
        return request.firstName().trim() + last;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String escapeLike(String term) {
        return term.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private ResponseStatusException alreadyRegistered() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Cliente já cadastrado nesta barbearia");
    }
}