package com.trustline.trustline.model;

import jakarta.persistence.*;
import jakarta.persistence.GeneratedValue;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;


    public User(String username) {
        this.username= username;
        this.password = password;
    }


}
