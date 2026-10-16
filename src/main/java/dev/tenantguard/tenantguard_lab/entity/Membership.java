package dev.tenantguard.tenantguard_lab.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "membership",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "organization_id"}
        )
)
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    protected Membership() {

    }

    public Membership(
            User user,
            Organization organization,
            Role role
    ) {
        this.user = user;
        this.organization = organization;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Organization getOrganization() {
        return organization;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
