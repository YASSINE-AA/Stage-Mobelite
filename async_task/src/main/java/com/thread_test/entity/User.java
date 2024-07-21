package com.thread_test.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="users")
public class User {

    @Id
    private Long identifier;
    private String username;
    private String first_name;
    private String last_name;

    public User() {}

    public User(Long identifier, String username, String first_name, String last_name) {
        this.identifier = identifier;
        this.username = username;
        this.first_name = first_name;
        this.last_name = last_name;
    }


    public void setIdentifier(Long identifier) {
        this.identifier = identifier;
    }

    public Long getIdentifier() {
        return identifier;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }
}
