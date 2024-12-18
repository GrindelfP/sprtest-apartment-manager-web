package to.grindelf.sprtest.utils.database;


import to.grindelf.sprtest.domain.User;

/**
 * Utility class for DatabaseDao.
 */
public class DatabaseDaoUtils {

    /**
     * Mapper for User object.
     * It maps the result set to a User object.
     */
    public final RowMapper<User> userMapper = rs -> new User(
            rs.getString("name"),
            rs.getString("password"),
            rs.getString("status")
    );

}
