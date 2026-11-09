
package com.nexoralabs.navalhahub.User.service;

import com.nexoralabs.navalhahub.User.dto.UserRequest;
import com.nexoralabs.navalhahub.User.dto.UserResponse;
import com.nexoralabs.navalhahub.barbershop.entity.Barbershop;
import com.nexoralabs.navalhahub.barbershop.entity.BarbershopMember;
import com.nexoralabs.navalhahub.barbershop.repository.BarbershopMemberRepository;
import com.nexoralabs.navalhahub.barbershop.repository.BarbershopRepository;
import com.nexoralabs.navalhahub.barbershop.util.RolesUtil;
import com.nexoralabs.navalhahub.identity.dto.IdentityUserResponse;
import com.nexoralabs.navalhahub.identity.mapper.IdentityUserMapper;
import com.nexoralabs.navalhahub.identity.service.IdentityService;
import com.nexoralabs.navalhahub.security.context.TenantContextHolder;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final IdentityService identityService;
    private final IdentityUserMapper identityUserMapper;

    private final BarbershopRepository barbershopRepository;
    private final BarbershopMemberRepository memberRepository;

    @Transactional
    public UserResponse createUser(
            String barbershopId,
            UserRequest userRequest
    ) {

        var context =
                TenantContextHolder.verifyBarberShopIdContext(barbershopId);

        RolesUtil.validateRoleAssignment(
                userRequest.role(),
                context.roles()
        );

        IdentityUserResponse identityResponse =
                identityService.createUser(
                        identityUserMapper.toIdentityUserRequest(userRequest)
                );

        try {

            Barbershop barbershop = barbershopRepository
                    .findById(barbershopId)
                    .orElseThrow(() ->
                            new EntityNotFoundException(
                                    "Barbershop not found"
                            )
                    );

            if (memberRepository.existsByIdentityIDAndBarbershop_Id(
                    identityResponse.identityId(),
                    barbershopId
            )) {
                throw new IllegalStateException(
                        "User already belongs to this barbershop"
                );
            }

            BarbershopMember member = BarbershopMember.builder()
                    .identityID(identityResponse.identityId())
                    .barbershop(barbershop)
                    .roles(new HashSet<>())
                    .active(true)
                    .build();

            member.getRoles().add(userRequest.role());

            memberRepository.saveAndFlush(member);

            return identityUserMapper.toUserResponse(identityResponse);

        } catch (RuntimeException exception) {

            try {
                identityService.deleteUser(
                        UUID.fromString(identityResponse.identityId())
                );
            } catch (RuntimeException cleanupException) {
                exception.addSuppressed(cleanupException);
            }

            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public UserResponse findUser(
            String barbershopId,
            UUID identityId
    ) {

        TenantContextHolder.verifyBarberShopIdContext(barbershopId);

        BarbershopMember member = findMembership(
                barbershopId,
                identityId
        );

        IdentityUserResponse identityResponse =
                identityService.findUser(identityId);

        return identityUserMapper.toUserResponse(identityResponse);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAllUsers(String barbershopId) {

        TenantContextHolder.verifyBarberShopIdContext(barbershopId);

        return memberRepository
                .findAllByBarbershop_IdAndActiveTrue(barbershopId)
                .stream()
                .map(member -> {
                    UUID identityId = UUID.fromString(
                            member.getIdentityID()
                    );

                    IdentityUserResponse identityResponse =
                            identityService.findUser(identityId);

                    return identityUserMapper.toUserResponse(
                            identityResponse
                    );
                })
                .toList();
    }

    @Transactional
    public UserResponse updateUser(
            String barbershopId,
            UUID identityId,
            UserRequest userRequest
    ) {

        var context =
                TenantContextHolder.verifyBarberShopIdContext(barbershopId);

        BarbershopMember member = findMembership(
                barbershopId,
                identityId
        );

        RolesUtil.validateRoleAssignment(
                userRequest.role(),
                context.roles()
        );

        IdentityUserResponse identityResponse =
                identityService.updateUser(
                        identityId,
                        identityUserMapper.toIdentityUserRequest(userRequest)
                );

        member.getRoles().clear();
        member.getRoles().add(userRequest.role());

        memberRepository.save(member);

        return identityUserMapper.toUserResponse(identityResponse);
    }

    @Transactional
    public void deleteUser(
            String barbershopId,
            UUID identityId
    ) {

        TenantContextHolder.verifyBarberShopIdContext(barbershopId);

        BarbershopMember member = findMembership(
                barbershopId,
                identityId
        );

        // Remove somente o vínculo com esta barbearia.
        // A identidade global permanece no Keycloak.

        memberRepository.delete(member);
    }

    private BarbershopMember findMembership(
            String barbershopId,
            UUID identityId
    ) {

        return memberRepository
                .findByIdentityIDAndBarbershop_Id(
                        identityId.toString(),
                        barbershopId
                )
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "User not found in this barbershop"
                        )
                );
    }
}
