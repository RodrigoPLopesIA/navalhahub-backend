package com.nexoralabs.navalhahub.User.service;

import com.nexoralabs.navalhahub.User.dto.TransferOwnershipRequest;
import com.nexoralabs.navalhahub.User.dto.UserRequest;
import com.nexoralabs.navalhahub.User.dto.UserResponse;
import com.nexoralabs.navalhahub.barbershop.entity.Barbershop;
import com.nexoralabs.navalhahub.barbershop.entity.BarbershopMember;
import com.nexoralabs.navalhahub.barbershop_member.repository.BarbershopMemberRepository;
import com.nexoralabs.navalhahub.barbershop.repository.BarbershopRepository;
import com.nexoralabs.navalhahub.barbershop.roles.BarbershopRole;
import com.nexoralabs.navalhahub.barbershop.util.RolesUtil;
import com.nexoralabs.navalhahub.identity.dto.IdentityUserResponse;
import com.nexoralabs.navalhahub.identity.mapper.IdentityUserMapper;
import com.nexoralabs.navalhahub.identity.service.IdentityService;
import com.nexoralabs.navalhahub.security.context.TenantContext;
import com.nexoralabs.navalhahub.security.context.TenantContextHolder;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final IdentityService identityService;
    private final IdentityUserMapper identityUserMapper;

    private final BarbershopRepository barbershopRepository;
    private final BarbershopMemberRepository memberRepository;

    // ================================================================
    // CREATE
    // ================================================================

    @Transactional
    public UserResponse createUser(String barbershopId, UserRequest userRequest) {

        var context = TenantContextHolder.verifyBarberShopIdContext(barbershopId);

        RolesUtil.validateRoleAssignment(userRequest.role(), context.roles());
        assertCanGrant(context, userRequest.role());

        IdentityUserResponse identityResponse = identityService.createUser(
                identityUserMapper.toIdentityUserRequest(userRequest));

        try {
            Barbershop barbershop = barbershopRepository
                    .findById(barbershopId)
                    .orElseThrow(() -> new EntityNotFoundException("Barbershop not found"));

            if (memberRepository.existsByIdentityIDAndBarbershop_Id(
                    identityResponse.identityId(), barbershopId)) {
                throw new IllegalStateException("User already belongs to this barbershop");
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
                identityService.deleteUser(UUID.fromString(identityResponse.identityId()));
            } catch (RuntimeException cleanupException) {
                exception.addSuppressed(cleanupException);
            }
            throw exception;
        }
    }

    // ================================================================
    // READ
    // ================================================================

    @Transactional(readOnly = true)
    public UserResponse findUser(String barbershopId, UUID identityId) {

        var context = TenantContextHolder.verifyBarberShopIdContext(barbershopId);
        BarbershopMember member = findMembership(barbershopId, identityId);

        boolean isSelf = context.identityId().equals(identityId);
        if (!isSelf && !isPrivileged(context.roles()) && isPrivileged(member.getRoles())) {
            throw new AccessDeniedException("You cannot access ADMIN or OWNER profiles");
        }

        return toResponse(identityId, member);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> findAllUsers(String barbershopId, Pageable pageable) {

        var context = TenantContextHolder.verifyBarberShopIdContext(barbershopId);

        boolean privileged = isPrivileged(context.roles());
        var restrictedRoles = List.of(BarbershopRole.ADMIN, BarbershopRole.OWNER);

        return memberRepository
                .findAllVisibleMembers(barbershopId, privileged, restrictedRoles, pageable)
                .map(member -> toResponse(UUID.fromString(member.getIdentityID()), member));
    }

    @Transactional(readOnly = true)
    public UserResponse findMe(String barbershopId) {

        var context = TenantContextHolder.verifyBarberShopIdContext(barbershopId);
        BarbershopMember member = findMembership(barbershopId, UUID.fromString(context.identityId()));

        return toResponse(UUID.fromString(context.identityId()), member);
    }

    // ================================================================
    // UPDATE
    // ================================================================

    /**
     * Edita os dados da propria identidade (nome, e-mail etc.).
     * O papel do usuario nunca muda por aqui, mesmo que venha no corpo.
     */
    @Transactional
    public UserResponse updateMe(String barbershopId, UserRequest userRequest) {

        var context = TenantContextHolder.verifyBarberShopIdContext(barbershopId);
        BarbershopMember member = findMembership(barbershopId, UUID.fromString(context.identityId()));

        IdentityUserResponse identityResponse = identityService.updateUser(
                UUID.fromString(context.identityId()),
                identityUserMapper.toIdentityUserRequest(userRequest));

        return identityUserMapper.toUserResponse(identityResponse)
                .withRoles(member.getRoles());
    }

    /**
     * BARBER <-> ADMIN. Quem age so altera quem esta abaixo dele e so concede
     * papeis abaixo do seu. OWNER nunca e atribuido aqui (ver transferOwnership).
     */
    @Transactional
    public UserResponse changeRole(String barbershopId, UUID identityId, BarbershopRole newRole) {

        var context = TenantContextHolder.verifyBarberShopIdContext(barbershopId);
        BarbershopMember target = findMembership(barbershopId, identityId);

        assertCanManage(context, target);
        assertCanGrant(context, newRole);

        target.getRoles().clear();
        target.getRoles().add(newRole);
        memberRepository.save(target);

        return toResponse(identityId, target);
    }

    @Transactional
    public UserResponse setActive(String barbershopId, UUID identityId, boolean active) {

        var context = TenantContextHolder.verifyBarberShopIdContext(barbershopId);
        BarbershopMember target = findMembership(barbershopId, identityId);

        assertCanManage(context, target);

        target.setActive(active);
        memberRepository.save(target);

        return toResponse(identityId, target);
    }

    // ================================================================
    // DELETE / LEAVE
    // ================================================================

    @Transactional
    public void deleteUser(String barbershopId, UUID identityId) {

        var context = TenantContextHolder.verifyBarberShopIdContext(barbershopId);
        BarbershopMember target = findMembership(barbershopId, identityId);

        // Para si mesmo, assertCanManage sempre nega: use leave().
        assertCanManage(context, target);

        // Remove somente o vinculo; a identidade global permanece no Keycloak.
        memberRepository.delete(target);
    }

    @Transactional
    public void leave(String barbershopId) {

        var context = TenantContextHolder.verifyBarberShopIdContext(barbershopId);
        BarbershopMember member = findMembership(barbershopId, UUID.fromString(context.identityId()));

        if (RolesUtil.isOwner(member.getRoles())) {
            throw new IllegalStateException(
                    "The owner must transfer ownership before leaving");
        }

        memberRepository.delete(member);
    }

    // ================================================================
    // OWNERSHIP
    // ================================================================

    /**
     * Promove o novo dono e rebaixa o atual para ADMIN na mesma transacao,
     * mantendo sempre exatamente um OWNER.
     */
    @Transactional
    public void transferOwnership(String barbershopId, TransferOwnershipRequest request) {

        var context = TenantContextHolder.verifyBarberShopIdContext(barbershopId);

        if (!RolesUtil.isOwner(context.roles())) {
            throw new AccessDeniedException("Only the owner can transfer ownership");
        }

        UUID newOwnerId = request.newOwnerIdentityId();
        if (newOwnerId.equals(UUID.fromString(context.identityId()))) {
            throw new IllegalArgumentException("You already own this barbershop");
        }

        BarbershopMember currentOwner = findMembership(barbershopId, UUID.fromString(context.identityId()));
        BarbershopMember newOwner = findMembership(barbershopId, newOwnerId);

        if (!newOwner.isActive()) {
            throw new IllegalStateException("The new owner must be an active member");
        }

        newOwner.getRoles().clear();
        newOwner.getRoles().add(BarbershopRole.OWNER);

        currentOwner.getRoles().clear();
        currentOwner.getRoles().add(BarbershopRole.ADMIN);

        memberRepository.saveAll(List.of(newOwner, currentOwner));
    }

    // ================================================================
    // Regras de hierarquia
    // ================================================================

    /** Quem age precisa ter papel estritamente maior que o do alvo. */
    private void assertCanManage(TenantContext context, BarbershopMember target) {
        if (highestRank(context.roles()) <= highestRank(target.getRoles())) {
            throw new AccessDeniedException(
                    "You can only manage members with a lower role than yours");
        }
    }

    /** OWNER nunca e concedido aqui; os demais so abaixo do papel de quem age. */
    private void assertCanGrant(TenantContext context, BarbershopRole role) {
        if (role == BarbershopRole.OWNER) {
            throw new AccessDeniedException(
                    "Ownership can only be changed through ownership transfer");
        }
        if (rank(role) >= highestRank(context.roles())) {
            throw new AccessDeniedException(
                    "You cannot assign a role equal to or higher than yours");
        }
    }

    private static int rank(BarbershopRole role) {
        return switch (role) {
            case BARBER -> 1;
            case ADMIN -> 2;
            case OWNER -> 3;
            default -> 0;
        };
    }

    private static int highestRank(Collection<BarbershopRole> roles) {
        return roles.stream().mapToInt(UserService::rank).max().orElse(0);
    }

    private boolean isPrivileged(Collection<BarbershopRole> roles) {
        return highestRank(roles) >= rank(BarbershopRole.ADMIN);
    }

    // ================================================================
    // Helpers
    // ================================================================

    private BarbershopMember findMembership(String barbershopId, UUID identityId) {
        return memberRepository
                .findByIdentityIDAndBarbershop_Id(identityId.toString(), barbershopId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found in this barbershop"));
    }

    private UserResponse toResponse(UUID identityId, BarbershopMember member) {
        return identityUserMapper
                .toUserResponse(identityService.findUser(identityId))
                .withRoles(member.getRoles());
    }
}