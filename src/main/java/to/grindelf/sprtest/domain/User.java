package to.grindelf.sprtest.domain;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class User {

    private String name;
    private String password;
    private UserStatus status;

    public User(@NotNull String name, @NotNull String password) {
        this.name = name;
        this.password = password;
        this.status = UserStatus.JUST_USER;
    }

    public User(@NotNull String name, @NotNull String password, @NotNull UserStatus status) {
        this.name = name;
        this.password = password;
        this.status = status;
    }

    public User(String name) {
        this.name = name;
        this.password = "no passwd";
        this.status = UserStatus.JUST_USER;
    }

    public User() {
        this.name = "no name";
        this.password = "no passwd";
        this.status = UserStatus.JUST_USER;
    }

    public User(String name, String password, String status) {
        this.name = name;
        this.password = password;
        if (Objects.equals(status, UserStatus.JUST_USER.toString())) this.status = UserStatus.JUST_USER;
        else this.status = UserStatus.ADMIN;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus userStatus) {
        this.status = userStatus;
    }

    @NotNull
    @Contract(pure = true)
    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' + ", " +
                "password='" + password + '\'' + ", " +
                "userStatus='" + status + '\'' +
                '}'
                ;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User)) return false;
        User userAsUser = (User) o;
        return (Objects.equals(this.name, userAsUser.name) && Objects.equals(this.password, userAsUser.password));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    public boolean isAdmin() {
        return this.getStatus() == UserStatus.ADMIN;
    }

    public boolean isJustUser() {
        return this.getStatus() == UserStatus.JUST_USER;
    }
}
