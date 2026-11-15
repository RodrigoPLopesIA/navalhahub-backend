
package com.nexoralabs.navalhahub.User.service;

import com.nexoralabs.navalhahub.User.dto.UserRequest;
import com.nexoralabs.navalhahub.User.dto.UserResponse;
import com.nexoralabs.navalhahub.barbershop.entity.Barbershop;
import com.nexoralabs.navalhahub.barbershop.entity.BarbershopMember;
import com.nexoralabs.navalhahub.barbershop.repository.BarbershopMemberRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import org.springframework.security.access.AccessDeniedException;

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
            UserRequest userRequest) {

        var context = TenantContextHolder.verifyBarberShopIdContext(barbershopId);

        RolesUtil.validateRoleAssignment(
                userRequest.role(),
                context.roles());

        IdentityUserResponse identityResponse = identityService.createUser(
                identityUserMapper.toIdentityUserRequest(userRequest));

        try {

            Barbershop barbershop = barbershopRepository
                    .findById(barbershopId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Barbershop not found"));

            if (memberRepository.existsByIdentityIDAndBarbershop_Id(
                    identityResponse.identityId(),
                    barbershopId)) {
                throw new IllegalStateException(
                        "User already belongs to this barbershop");
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
                        UUID.fromString(identityResponse.identityId()));
            } catch (RuntimeException cleanupException) {
                exception.addSuppressed(cleanupException);
            }

            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public UserResponse findUser(
            String barbershopId,
            UUID identityId) {

        var context = TenantContextHolder.verifyBarberShopIdContext(barbershopId);

        BarbershopMember member = findMembership(
                barbershopId,
                identityId);
        boolean requesterIsManager = RolesUtil.isAdmin(context.roles()) || RolesUtil.isOwner(context.roles());
        boolean targetIsManager = RolesUtil.isAdmin(member.getRoles()) || RolesUtil.isOwner(member.getRoles());

        if (!requesterIsManager && targetIsManager) {
            throw new AccessDeniedException(
                    "You cannot access ADMIN or OWNER profiles");
        }

        IdentityUserResponse identityResponse = identityService.findUser(identityId);

        return identityUserMapper.toUserResponse(identityResponse).withRoles(member.getRoles());
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> findAllUsers(
            String barbershopId,
            Pageable pageable
    ) {
        var context = TenantContextHolder.verifyBarberShopIdContext(barbershopId);

        boolean isPrivileged = context.roles().stream()
                .anyMatch(role -> Set.of(
                        BarbershopRole.ADMIN,
                        BarbershopRole.OWNER
                ).contains(role));

        var restrictedRoles = List.of(
                BarbershopRole.ADMIN,
                BarbershopRole.OWNER
        );

        return memberRepository.findAllVisibleMembers(
                barbershopId,
                isPrivileged,
                restrictedRoles,
                pageable
        ).map(member -> {
            var identityId = UUID.fromString(member.getIdentityID());

            var identityResponse = identityService.findUser(identityId);

            return identityUserMapper.toUserResponse(identityResponse)
                    .withRoles(member.getRoles());
        });
    }

    @Transactional
    public UserResponse updateUser(
            String barbershopId,
            UUID identityId,
            UserRequest userRequest
    ) {
        var context = TenantContextHolder.verifyBarberShopIdContext(barbershopId);

        validateProfileAccess(identityId, context);

        var member = findMembership(barbershopId, identityId);

        RolesUtil.validateRoleAssignment(
                userRequest.role(),
                context.roles()
        );

        var identityResponse = identityService.updateUser(
                identityId,
                identityUserMapper.toIdentityUserRequest(userRequest)
        );

        member.getRoles().clear();
        member.getRoles().add(userRequest.role());

        memberRepository.save(member);

        return identityUserMapper.toUserResponse(identityResponse)
                .withRoles(member.getRoles());
    }

    @Transactional
    public void deleteUser(
            String barbershopId,
            UUID identityId
    ) {
        var context = TenantContextHolder.verifyBarberShopIdContext(barbershopId);

        validateProfileAccess(identityId, context);

        var member = findMembership(barbershopId, identityId);

        // Remove somente o vínculo com esta barbearia.
        // A identidade global permanece no Keycloak.

        memberRepository.delete(member);
    }

    private BarbershopMember findMembership(
            String barbershopId,
            UUID identityId) {

        return memberRepository
                .findByIdentityIDAndBarbershop_Id(
                        identityId.toString(),
                        barbershopId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found in this barbershop"));
    }

    private boolean isPrivileged(Collection<BarbershopRole> roles) {
        return roles.stream().anyMatch(role ->
                Set.of(
                        BarbershopRole.ADMIN,
                        BarbershopRole.OWNER
                ).contains(role)
        );
    }

    private void validateProfileAccess(
            UUID targetIdentityId,
            TenantContext context
    ) {
        if (!isPrivileged(context.roles())
                && !context.identityId().equals(targetIdentityId)) {
            throw new AccessDeniedException(
                    "You can only manage your own profile"
            );
        }
    }
}
