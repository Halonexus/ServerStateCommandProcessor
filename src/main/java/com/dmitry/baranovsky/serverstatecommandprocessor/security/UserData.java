package com.dmitry.baranovsky.serverstatecommandprocessor.security;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Class for representing registered web service users.
 */
@Entity
@Table(name = "users")
public class UserData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    @Setter
    @Column(name = "uid")
    private int id;

    @Getter
    @Setter
    @Column(name = "user")
    private String userName;
    @Getter
    @Setter
    @Column(name = "password")
    private String passwordHash;

    public UserData() {
    }

    public UserData(String userName, String passwordHash) {
        this.userName = userName;
        this.passwordHash = passwordHash;
    }

    public UserData(int id, String userName, String passwordHash) {
        this.id = id;
        this.userName = userName;
        this.passwordHash = passwordHash;
    }
}