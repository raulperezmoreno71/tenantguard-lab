package dev.tenantguard.tenantguard_lab.service;

import dev.tenantguard.tenantguard_lab.entity.Membership;
import dev.tenantguard.tenantguard_lab.entity.Organization;
import dev.tenantguard.tenantguard_lab.entity.Role;
import dev.tenantguard.tenantguard_lab.entity.User;
import dev.tenantguard.tenantguard_lab.exception.ConflictException;
import dev.tenantguard.tenantguard_lab.exception.ResourceNotFoundException;
import dev.tenantguard.tenantguard_lab.repository.MembershipRepository;
import dev.tenantguard.tenantguard_lab.repository.OrganizationRepository;
import dev.tenantguard.tenantguard_lab.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    public MembershipService (MembershipRepository membershipRepository, UserRepository userRepository, OrganizationRepository organizationRepository) {
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
    }

    private Membership getMembership(Long userId, Long organizationId) {
        return membershipRepository.findByUser_IdAndOrganization_Id(userId, organizationId)
                .orElseThrow(() -> new AccessDeniedException("Access denied."));
    }

    public void requireRole(Long userId, Long organizationId, Role requiredRole) {
        Membership membership = getMembership(userId, organizationId);

        switch (membership.getRole()){
            case Role.VIEWER -> {
                if (requiredRole != Role.VIEWER) {
                    throw new AccessDeniedException("Access denied.");
                }
            }
            case Role.EDITOR -> {
                if (requiredRole == Role.ADMIN) {
                    throw new AccessDeniedException("Access denied.");
                }
            }
            case Role.ADMIN -> {
                break;
            }
        }
    }

    @Transactional
    public Membership addMember(Long organizationId, Long adminId, String username, Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role must not be null.");
        }

        Organization organization = lockOrganization(organizationId);
        requireRole(adminId, organizationId, Role.ADMIN);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (membershipRepository.findByUser_IdAndOrganization_Id(user.getId(), organizationId).isPresent()) {
            throw new ConflictException("User already belongs to this organization.");
        }

        Membership membership = new Membership(user, organization, role);

        return membershipRepository.save(membership);
    }

    @Transactional(readOnly = true)
    public List<Membership> getMembers(Long organizationId, Long adminId) {
        requireRole(adminId, organizationId, Role.ADMIN);
        return membershipRepository.findByOrganization_IdOrderByIdAsc(organizationId);
    }

    @Transactional
    public Membership updateRole(Long organizationId, Long memberId, Long adminId, Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role must not be null.");
        }
        lockOrganization(organizationId);
        requireRole(adminId, organizationId, Role.ADMIN);
        Membership member = findMember(organizationId, memberId);
        ensureAdministratorRemains(organizationId, member, role);
        member.setRole(role);
        return member;
    }

    @Transactional
    public void removeMember(Long organizationId, Long memberId, Long adminId) {
        lockOrganization(organizationId);
        requireRole(adminId, organizationId, Role.ADMIN);
        Membership member = findMember(organizationId, memberId);
        ensureAdministratorRemains(organizationId, member, null);
        membershipRepository.delete(member);
    }

    private Organization lockOrganization(Long organizationId) {
        return organizationRepository.findByIdForUpdate(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found."));
    }

    private Membership findMember(Long organizationId, Long memberId) {
        return membershipRepository.findByOrganization_IdAndId(organizationId, memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found."));
    }

    private void ensureAdministratorRemains(Long organizationId, Membership member, Role nextRole) {
        if (member.getRole() == Role.ADMIN && nextRole != Role.ADMIN
                && membershipRepository.countByOrganization_IdAndRole(organizationId, Role.ADMIN) <= 1) {
            throw new ConflictException("Organization must have at least one administrator.");
        }
    }
}
