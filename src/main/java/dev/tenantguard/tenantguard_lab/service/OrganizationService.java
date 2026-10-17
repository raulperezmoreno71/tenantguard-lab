package dev.tenantguard.tenantguard_lab.service;

import dev.tenantguard.tenantguard_lab.entity.Membership;
import dev.tenantguard.tenantguard_lab.entity.Organization;
import dev.tenantguard.tenantguard_lab.entity.Role;
import dev.tenantguard.tenantguard_lab.entity.User;
import dev.tenantguard.tenantguard_lab.exception.OrganizationAlreadyExistsException;
import dev.tenantguard.tenantguard_lab.repository.MembershipRepository;
import dev.tenantguard.tenantguard_lab.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final MembershipRepository membershipRepository;

    public OrganizationService (OrganizationRepository organizationRepository, MembershipRepository membershipRepository) {
        this.organizationRepository = organizationRepository;
        this.membershipRepository = membershipRepository;
    }

    @Transactional
    public Organization createOrganization(String name, User creator) {

        if (name == null) {
            throw new IllegalArgumentException("The name must not ve null");
        }
        String nameTrim = name.trim();

        if (nameTrim.isEmpty()) {
            throw new IllegalArgumentException("The name must not be null or empty.");
        }

        if (organizationRepository.findByName(nameTrim).isPresent()) {
            throw new OrganizationAlreadyExistsException();
        }

        Organization organization = organizationRepository.save(new Organization(nameTrim));

        membershipRepository.save(new Membership(creator, organization, Role.ADMIN));

        return organization;
    }
}
