package dev.tenantguard.tenantguard_lab.config;

import dev.tenantguard.tenantguard_lab.entity.Organization;
import dev.tenantguard.tenantguard_lab.entity.Role;
import dev.tenantguard.tenantguard_lab.entity.User;
import dev.tenantguard.tenantguard_lab.repository.DocumentRepository;
import dev.tenantguard.tenantguard_lab.repository.MembershipRepository;
import dev.tenantguard.tenantguard_lab.repository.OrganizationRepository;
import dev.tenantguard.tenantguard_lab.repository.UserRepository;
import dev.tenantguard.tenantguard_lab.service.AuthService;
import dev.tenantguard.tenantguard_lab.service.DocumentService;
import dev.tenantguard.tenantguard_lab.service.MembershipService;
import dev.tenantguard.tenantguard_lab.service.OrganizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("lab")
public class LabDataInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(LabDataInitializer.class);

    private final UserRepository users;
    private final OrganizationRepository organizations;
    private final MembershipRepository memberships;
    private final DocumentRepository documents;
    private final AuthService authService;
    private final OrganizationService organizationService;
    private final MembershipService membershipService;
    private final DocumentService documentService;
    private final String alicePassword;
    private final String bobPassword;
    private final String charliePassword;
    private final String davidPassword;
    private final String evePassword;

    public LabDataInitializer(
            UserRepository users,
            OrganizationRepository organizations,
            MembershipRepository memberships,
            DocumentRepository documents,
            AuthService authService,
            OrganizationService organizationService,
            MembershipService membershipService,
            DocumentService documentService,
            @Value("${lab.password.alice}") String alicePassword,
            @Value("${lab.password.bob}") String bobPassword,
            @Value("${lab.password.charlie}") String charliePassword,
            @Value("${lab.password.david}") String davidPassword,
            @Value("${lab.password.eve}") String evePassword
    ) {
        this.users = users;
        this.organizations = organizations;
        this.memberships = memberships;
        this.documents = documents;
        this.authService = authService;
        this.organizationService = organizationService;
        this.membershipService = membershipService;
        this.documentService = documentService;
        this.alicePassword = alicePassword;
        this.bobPassword = bobPassword;
        this.charliePassword = charliePassword;
        this.davidPassword = davidPassword;
        this.evePassword = evePassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.count() != 0 || organizations.count() != 0
                || memberships.count() != 0 || documents.count() != 0) {
            log.info("Lab initializer skipped because the database already contains domain data.");
            return;
        }

        validatePassword("ALICE", alicePassword);
        validatePassword("BOB", bobPassword);
        validatePassword("CHARLIE", charliePassword);
        validatePassword("DAVID", davidPassword);
        validatePassword("EVE", evePassword);

        User alice = authService.register("alice", alicePassword);
        User bob = authService.register("bob", bobPassword);
        User charlie = authService.register("charlie", charliePassword);
        User david = authService.register("david", davidPassword);
        User eve = authService.register("eve", evePassword);

        Organization acme = organizationService.createOrganization("Acme Corp", alice);
        Organization globex = organizationService.createOrganization("Globex Corp", david);

        membershipService.addMember(acme.getId(), alice.getId(), bob.getUsername(), Role.EDITOR);
        membershipService.addMember(acme.getId(), alice.getId(), charlie.getUsername(), Role.VIEWER);
        membershipService.addMember(globex.getId(), david.getId(), eve.getUsername(), Role.VIEWER);

        documentService.createDocument(acme.getId(), alice.getId(),
                "ACME-001 - Plan de seguridad", "ACME CORP: plan ficticio de seguridad. Solo Acme puede leer este documento.");
        documentService.createDocument(acme.getId(), bob.getId(),
                "ACME-002 - Informe de auditoría", "ACME CORP: informe ficticio de auditoría preparado por Bob.");
        documentService.createDocument(acme.getId(), alice.getId(),
                "ACME-003 - Inventario de activos", "ACME CORP: inventario ficticio de activos para pruebas de aislamiento.");

        documentService.createDocument(globex.getId(), david.getId(),
                "GLOBEX-001 - Plan de seguridad", "GLOBEX CORP: plan ficticio de seguridad. Solo Globex puede leer este documento.");
        documentService.createDocument(globex.getId(), david.getId(),
                "GLOBEX-002 - Informe de auditoría", "GLOBEX CORP: informe ficticio de auditoría independiente de Acme.");
        documentService.createDocument(globex.getId(), david.getId(),
                "GLOBEX-003 - Inventario de activos", "GLOBEX CORP: inventario ficticio de activos para pruebas de aislamiento.");

        log.info("Lab scenario initialized: 5 users, 2 organizations, 5 memberships and 6 documents.");
    }

    private void validatePassword(String username, String password) {
        if (password == null || password.isBlank() || password.length() < 12 || password.length() > 72) {
            throw new IllegalStateException("LAB_PASSWORD_" + username + " must contain 12 to 72 characters.");
        }
    }
}
