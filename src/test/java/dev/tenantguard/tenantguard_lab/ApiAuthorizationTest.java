package dev.tenantguard.tenantguard_lab;

import dev.tenantguard.tenantguard_lab.entity.Document;
import dev.tenantguard.tenantguard_lab.entity.Membership;
import dev.tenantguard.tenantguard_lab.entity.Organization;
import dev.tenantguard.tenantguard_lab.entity.Role;
import dev.tenantguard.tenantguard_lab.entity.User;
import dev.tenantguard.tenantguard_lab.repository.DocumentRepository;
import dev.tenantguard.tenantguard_lab.repository.MembershipRepository;
import dev.tenantguard.tenantguard_lab.repository.OrganizationRepository;
import dev.tenantguard.tenantguard_lab.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class ApiAuthorizationTest {
    @Autowired WebApplicationContext context;
    @Autowired UserRepository users;
    @Autowired OrganizationRepository organizations;
    @Autowired MembershipRepository memberships;
    @Autowired DocumentRepository documents;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void documentRoutesEnforceRolesAndTenantScope() throws Exception {
        User admin = users.save(new User("api-admin-doc", "hash"));
        User editor = users.save(new User("api-editor-doc", "hash"));
        User viewer = users.save(new User("api-viewer-doc", "hash"));
        User outsider = users.save(new User("api-outsider-doc", "hash"));
        Organization first = organizations.save(new Organization("API Docs First"));
        Organization second = organizations.save(new Organization("API Docs Second"));
        memberships.save(new Membership(admin, first, Role.ADMIN));
        memberships.save(new Membership(editor, first, Role.EDITOR));
        memberships.save(new Membership(viewer, first, Role.VIEWER));
        memberships.save(new Membership(outsider, second, Role.ADMIN));
        Document document = documents.save(new Document("Original", "Body", first, admin));
        String url = "/api/organizations/" + first.getId() + "/documents/" + document.getId();

        mvc.perform(get(url)).andExpect(status().isUnauthorized());
        mvc.perform(get(url).with(token(outsider))).andExpect(status().isForbidden());
        mvc.perform(get(url).with(token(viewer))).andExpect(status().isOk());
        mvc.perform(put(url).with(token(viewer)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Changed\",\"content\":\"Body\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(put(url).with(token(editor)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Changed\",\"content\":\"Body\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Changed"));
        mvc.perform(get("/api/organizations/" + second.getId() + "/documents/" + document.getId())
                .with(token(outsider))).andExpect(status().isNotFound());
        mvc.perform(delete(url).with(token(editor))).andExpect(status().isNoContent());
    }

    @Test
    void memberRoutesPreserveAnAdministratorAndValidateRole() throws Exception {
        User admin = users.save(new User("api-admin-members", "hash"));
        User viewer = users.save(new User("api-viewer-members", "hash"));
        Organization organization = organizations.save(new Organization("API Members"));
        Membership adminMember = memberships.save(new Membership(admin, organization, Role.ADMIN));
        Membership viewerMember = memberships.save(new Membership(viewer, organization, Role.VIEWER));
        String base = "/api/organizations/" + organization.getId() + "/members";

        mvc.perform(get(base).with(token(viewer))).andExpect(status().isForbidden());
        mvc.perform(get(base).with(token(admin))).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("ADMIN"));
        mvc.perform(put(base + "/" + viewerMember.getId() + "/role").with(token(admin))
                .contentType(MediaType.APPLICATION_JSON).content("{\"role\":null}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put(base + "/" + adminMember.getId() + "/role").with(token(admin))
                .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"EDITOR\"}"))
                .andExpect(status().isConflict());
        mvc.perform(delete(base + "/" + adminMember.getId()).with(token(admin)))
                .andExpect(status().isConflict());
        mvc.perform(delete(base + "/" + viewerMember.getId()).with(token(admin)))
                .andExpect(status().isNoContent());
    }

    @Test
    void viewerCannotPromoteThemselvesToAdmin() throws Exception {
        User admin = users.save(new User("api-admin-bfla", "hash"));
        User viewer = users.save(new User("api-viewer-bfla", "hash"));

        Organization organization = organizations.save(
                new Organization("API BFLA Test")
        );

        memberships.save(new Membership(admin, organization, Role.ADMIN));

        Membership viewerMember = memberships.save(
                new Membership(viewer, organization, Role.VIEWER)
        );

        String url = "/api/organizations/" + organization.getId()
                + "/members/" + viewerMember.getId() + "/role";

        mvc.perform(put(url)
                        .with(token(viewer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isForbidden());

        Membership updatedMember = memberships.findById(viewerMember.getId())
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                Role.VIEWER,
                updatedMember.getRole()
        );
    }

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token(User user) {
        return SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.subject(user.getId().toString()));
    }
}
