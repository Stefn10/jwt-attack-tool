package rs.raf.novosti.entities;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class User {

    private Long id;

    @NotNull(message = "Email field is required")
    @NotEmpty(message = "Email field is required")
    private String email;

    @NotNull(message = "Name field is required")
    @NotEmpty(message = "Name field is required")
    private String name;

    @NotNull(message = "Surname field is required")
    @NotEmpty(message = "Surname field is required")
    private String surname;

    @NotNull(message = "User type field is required")
    private UserType userType;

    private boolean status = true;

    private String hashedPassword;

    public User() {
    }

    public User(Long id, String email, String name, String surname, UserType userType, boolean status, String hashedPassword) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.userType = userType;
        this.status = status;
        this.hashedPassword = hashedPassword;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }
}
