package co.ravn.ecommerce.Models.Users;

public class UserRegisterRequest {

    private String username;
    private String password;
    private int roleId;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getRoleId() {
        return roleId;
    }

    @Override
    public String toString() {
        return "InputRegisterUser{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", roleId=" + roleId +
                '}';
    }
}
