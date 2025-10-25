package com.trustline.trustline.appuser.model;

import jakarta.persistence.*;
import jakarta.persistence.GeneratedValue;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@ToString
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User extends AuditModel{
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;
    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;
    private String googleId;
    private String phoneNumber;
    private boolean deleted;
    private String firstName;
    private String lastName;
    private Gender gender;
    @Enumerated(EnumType.STRING)
    private Status status;
    private boolean accountVerified;
    private String password;
    private String profileImageUrl;

    @ManyToMany
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

}
