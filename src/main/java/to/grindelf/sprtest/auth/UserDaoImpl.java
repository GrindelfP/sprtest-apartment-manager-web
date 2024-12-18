package to.grindelf.sprtest.auth;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import to.grindelf.sprtest.domain.User;
import to.grindelf.sprtest.exceptions.NoSuchUserException;
import to.grindelf.sprtest.exceptions.UserAlreadyExistsException;
import to.grindelf.sprtest.utils.DataOperator;
import to.grindelf.sprtest.utils.database.DatabaseDaoUtils;
import to.grindelf.sprtest.utils.database.SQLOperator;
import to.grindelf.sprtest.utils.json.JsonOperator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static to.grindelf.sprtest.utils.ConstantValues.*;

@Component
public class UserDaoImpl implements UserDao<User> {

    private final DataOperator<User, String> operator;

    public UserDaoImpl() {
        this.operator = new SQLOperator<>();
    }

    public UserDaoImpl(DataOperator<User, String> operator) {
        this.operator = operator;
    }

    @Override
    public User getUserByName(@NotNull String userName) throws NoSuchUserException {

        User resultUser = null;

        if (this.operator instanceof SQLOperator<User, String>) {
            resultUser = sqlGetUserByName(userName);
        } else if (this.operator instanceof JsonOperator<User, String>) {
            resultUser = jsonGetUserByName(userName);
        }

        return resultUser;
    }

    private User sqlGetUserByName(@NotNull String userName) throws NoSuchUserException {
        try {
            return this.operator.getByKey(
                    userName,
                    USERS_COLUMN_KEY_COLUMN_NAME,
                    USER_DB_FILE_PATH,
                    DatabaseTableNames.USERS_TABLE,
                    new DatabaseDaoUtils().userMapper
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private User jsonGetUserByName(@NotNull String userName) throws NoSuchUserException {
        List<User> userList = this.operator.readFile(USER_JSON_FILE_PATH);

        User resultUser = null;
        for (User user : userList) {
            if (Objects.equals(user.getName(), userName)) {
                resultUser = user;
                break;
            }
        }
        if (!checkIfUserExists(userName, userList)) {
            throw new NoSuchUserException();
        } else
            return resultUser;
    }

    @Override
    public List<User> getAll() {

        List<User> users = new ArrayList<>();

        if (this.operator instanceof SQLOperator<User, String>) {
            users = sqlGetAll();
        } else if (this.operator instanceof JsonOperator<User, String>) {
            users = jsonGetAll();
        }

        return users;
    }

    private @NotNull List<User> sqlGetAll() {
        try {
            return this.operator.getAll(
                    USER_DB_FILE_PATH,
                    new DatabaseDaoUtils().userMapper,
                    DatabaseTableNames.USERS_TABLE
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<User> jsonGetAll() {
        return this.operator.readFile(USER_JSON_FILE_PATH);
    }

    @Override
    public void save(@NotNull User user) throws UserAlreadyExistsException {

        if (this.operator instanceof SQLOperator<User, String>) {
            sqlSave(user);
        } else if (this.operator instanceof JsonOperator<User, String>) {
            jsonSave(user);
        }
    }

    private void jsonSave(@NotNull User user) throws UserAlreadyExistsException {
        List<User> userList = this.operator.readFile(USER_JSON_FILE_PATH);

        if (checkIfUserExists(user.getName(), userList)) {
            throw new UserAlreadyExistsException();
        } else {
            userList.add(user);
        }

        this.operator.writeToFile(USER_JSON_FILE_PATH, userList);
    }

    private void sqlSave(@NotNull User user) throws UserAlreadyExistsException {
        try {
            this.operator.post(
                    user,
                    USER_DB_FILE_PATH,
                    DatabaseTableNames.USERS_TABLE,
                    USERS_COLUMN_KEY_COLUMN_NAME
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(@NotNull User user) throws NoSuchUserException {

        if (this.operator instanceof SQLOperator<User, String>) {
            sqlUpdate(user);
        } else if (this.operator instanceof JsonOperator<User, String>) {
            jsonUpdate(user);
        }
    }

    private void sqlUpdate(@NotNull User user) throws NoSuchUserException {
        try {
            this.operator.update(
                    user.getName(),
                    USERS_COLUMN_KEY_COLUMN_NAME,
                    user,
                    USER_DB_FILE_PATH,
                    DatabaseTableNames.USERS_TABLE
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void jsonUpdate(@NotNull User user) throws NoSuchUserException {
        List<User> userList = this.operator.readFile(USER_JSON_FILE_PATH);

        if (!checkIfUserExists(user.getName(), userList)) {
            throw new NoSuchUserException();
        } else {
            for (int i = 0; i < userList.size(); i++) {
                if (Objects.equals(userList.get(i).getName(), user.getName())) {
                    userList.set(i, user);
                    break;
                }
            }
        }

        this.operator.writeToFile(USER_JSON_FILE_PATH, userList);
    }

    @Override
    public void delete(@NotNull String userName) throws NoSuchUserException {

        if (this.operator instanceof SQLOperator<User, String>) {
            sqlDelete(userName);
        } else if (this.operator instanceof JsonOperator<User, String>) {
            jsonDelete(userName);
        }
    }

    private void jsonDelete(@NotNull String userName) throws NoSuchUserException {
        List<User> userList = this.operator.readFile(USER_JSON_FILE_PATH);

        if (!checkIfUserExists(userName, userList)) {
            throw new NoSuchUserException();
        } else {
            for (int i = 0; i < userList.size(); i++) {
                if (Objects.equals(userList.get(i).getName(), userName)) {
                    userList.remove(i);
                    break;
                }
            }
        }

        this.operator.writeToFile(USER_JSON_FILE_PATH, userList);
    }

    private void sqlDelete(@NotNull String userName) throws NoSuchUserException {
        try {
            this.operator.delete(
                    userName,
                    USERS_COLUMN_KEY_COLUMN_NAME,
                    USER_DB_FILE_PATH,
                    DatabaseTableNames.USERS_TABLE
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkIfUserExists(@NotNull String userName, @NotNull List<User> userList) {
        boolean exists = false;
        for (User currentUser : userList) {
            if (Objects.equals(userName, currentUser.getName())) {
                exists = true;
                break;
            }
        }

        return exists;
    }
}
