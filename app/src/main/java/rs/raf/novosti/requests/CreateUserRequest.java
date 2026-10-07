package rs.raf.novosti.requests;

import rs.raf.novosti.entities.UserType;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class CreateUserRequest {

    @NotNull(message = "Email is required")
    @NotEmpty(message = "Email is required")
    private String email;

    @NotNull(message = "Name is required")
    @NotEmpty(message = "Name is required")
    private String name;

    @NotNull(message = "Surname is required")
    @NotEmpty(message = "Surname is required")
    private String surname;

    @NotNull(message = "User type is required")
    private UserType userType;

    @NotNull(message = "Password is required")
    @NotEmpty(message = "Password is required")
    private String password;

    @NotNull(message = "Password confirmation is required")
    @NotEmpty(message = "Password confirmation is required")
    private String confirmPassword;

    public CreateUserRequest() {
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
