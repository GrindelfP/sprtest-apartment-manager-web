package to.grindelf.sprtest.utils.database;

import org.jetbrains.annotations.NotNull;
import to.grindelf.sprtest.annonations.SQLPurposed;
import to.grindelf.sprtest.exceptions.IrregularAccessException;
import to.grindelf.sprtest.exceptions.JSONException;
import to.grindelf.sprtest.exceptions.NoSuchUserException;
import to.grindelf.sprtest.exceptions.UserAlreadyExistsException;
import to.grindelf.sprtest.utils.ConstantValues;
import to.grindelf.sprtest.utils.DataOperator;


import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that handles SQL operations.
 *
 * @param <T> type of the object to operate on.
 * @param <K> type of the object to use as a key to get other objects.
 */
public class SQLOperator<T, K> implements DataOperator<T, K> {

    public SQLOperator() {}

    // ===================================================================== \\
    //                 HIGH-LEVEL SQL OPERATIONS WITH DATA                   \\
    // These are the high-level operations that are used by the application. \\
    // ===================================================================== \\

    @Override
    @SQLPurposed
    public T getByKey(
            @NotNull K key,
            @NotNull String keyColumnName,
            @NotNull String filePath,
            @NotNull ConstantValues.DatabaseTableNames tableName,
            @NotNull RowMapper<T> mapper
    ) throws SQLException, IrregularAccessException, NoSuchUserException {
        String url = "jdbc:sqlite:" + filePath;
        String query = "SELECT * FROM " + tableName + " WHERE " + keyColumnName + " = ?";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setObject(1, key);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    T result = mapper.mapRow(rs);

                    if (rs.next()) {
                        throw new SQLException("Database inconsistency: multiple rows found for key " + key);
                    }

                    return result;
                } else {
                    throw new NoSuchUserException();
                }
            }
        }
    }

    /**
     * Returns all data from the table in the database file.
     *
     * @param filePath  path to the database file
     * @param mapper    mapper to convert ResultSet to object
     * @param tableName name of the table to get data from
     * @return list of objects of type T
     */
    @Override
    @SQLPurposed
    public @NotNull List<T> getAll(
            @NotNull String filePath,
            @NotNull RowMapper<T> mapper,
            @NotNull ConstantValues.DatabaseTableNames tableName
    ) throws SQLException, IrregularAccessException {

        List<T> result = new ArrayList<>();
        String url = "jdbc:sqlite:" + filePath;

        try (Connection conn = DriverManager.getConnection(url)) {
            String query = "SELECT * FROM " + tableName;

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    result.add(mapper.mapRow(rs));
                }
            }
        }

        return result;
    }

    /**
     * Validates and, if valid, inserts data into the database file.
     *
     * @param data          object to insert
     * @param filePath      path to the database file
     * @param tableName     name of the table to insert data into
     * @param keyColumnName
     */
    @Override
    @SQLPurposed
    public void post(
            @NotNull T data,
            @NotNull String filePath,
            @NotNull ConstantValues.DatabaseTableNames tableName,
            @NotNull String keyColumnName
    ) throws SQLException, IrregularAccessException, UserAlreadyExistsException {
        String url = "jdbc:sqlite:" + filePath;

        try (Connection conn = DriverManager.getConnection(url)) {
            validateDataAgainstTableStructure(data, conn, tableName.toString());

            Field nameField = data.getClass().getDeclaredField(keyColumnName);
            nameField.setAccessible(true);
            K nameValue = (K) nameField.get(data);

            String checkQuery = "SELECT COUNT(*) FROM " + tableName + " WHERE " + keyColumnName + " = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkQuery)) {
                stmt.setString(1, nameValue.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new UserAlreadyExistsException();
                    }
                }
            }

            insertQuery(data, conn, tableName.toString());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to access " + keyColumnName + " field in data object.", e);
        }
    }

    /**
     * Updates the data in the database file.
     *
     * @param key           key to update the object by
     * @param keyColumnName name of the column that is used as a key
     * @param filePath      path to the database file
     * @param tableName     name of the table to update data in
     * @throws SQLException             if an error occurs during the operation
     * @throws IrregularAccessException if an error occurs during the operation
     */
    @Override
    @SQLPurposed
    public void update(
            @NotNull K key,
            @NotNull String keyColumnName,
            @NotNull T data,
            @NotNull String filePath,
            @NotNull ConstantValues.DatabaseTableNames tableName
    ) throws SQLException, IrregularAccessException, NoSuchUserException {
        String url = "jdbc:sqlite:" + filePath;

        try (Connection conn = DriverManager.getConnection(url)) {
            // Проверка на существование записи
            String checkQuery = "SELECT COUNT(*) FROM " + tableName + " WHERE " + keyColumnName + " = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setObject(1, key);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) == 0) {
                        throw new NoSuchUserException();
                    }
                }
            }

            validateDataAgainstTableStructure(data, conn, tableName.toString());
            updateQuery(key, keyColumnName, data, conn, tableName.toString());
        }
    }



    /**
     * Deletes the data from the database file.
     *
     * @param key           key to delete the object by
     * @param keyColumnName name of the column that is used as a key
     * @param filePath      path to the database file
     * @param tableName     name of the table to delete data from
     * @throws SQLException if an error occurs during the operation
     */
    @Override
    @SQLPurposed
    public void delete(
            @NotNull K key,
            @NotNull String keyColumnName,
            @NotNull String filePath,
            @NotNull ConstantValues.DatabaseTableNames tableName
    ) throws SQLException, IrregularAccessException, NoSuchUserException {
        String url = "jdbc:sqlite:" + filePath;

        try (Connection conn = DriverManager.getConnection(url)) {
            // Проверка на существование записи
            String checkQuery = "SELECT COUNT(*) FROM " + tableName + " WHERE " + keyColumnName + " = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setObject(1, key);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) == 0) {
                        throw new NoSuchUserException();
                    }
                }
            }

            String query = "DELETE FROM " + tableName + " WHERE " + keyColumnName + " = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setObject(1, key);
                stmt.executeUpdate();
            }
        }
    }

    // =================================================================== \\
    //                 LOW-LEVEL SQL OPERATIONS WITH DATA                  \\
    // These are the helper-functions for the high-level operations above. \\
    // =================================================================== \\

    /**
     * Validates the data against the table structure.
     *
     * @param data      object to validate
     * @param conn      database connection
     * @param tableName name of the table to validate against
     * @throws SQLException if an error occurs during the validation process
     */
    private static <T> void validateDataAgainstTableStructure(
            @NotNull T data,
            @NotNull Connection conn,
            @NotNull String tableName
    ) throws SQLException {
        // Retrieve database table structure
        DatabaseMetaData metaData = conn.getMetaData();
        Map<String, String> tableColumns = new HashMap<>();

        try (ResultSet columns = metaData.getColumns(null, null, tableName, null)) {
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String columnType = columns.getString("TYPE_NAME");
                tableColumns.put(columnName, columnType);
            }
        }

        // Retrieve object's field structure
        Field[] fields = data.getClass().getDeclaredFields();

        for (Field field : fields) {
            String fieldName = field.getName();
            field.setAccessible(true);

            if (!tableColumns.containsKey(fieldName)) {
                throw new IllegalArgumentException("Field '" + fieldName + "' is not present in table '" + tableName + "'.");
            }
        }
    }

    /**
     * Inserts data into the database table
     *
     * @param data      object to insert
     * @param conn      database connection
     * @param tableName name of the table to insert data into
     * @throws SQLException if an error occurs during the insert operation
     */
    private static <T> void insertQuery(
            @NotNull T data,
            @NotNull Connection conn,
            @NotNull String tableName
    ) throws SQLException {
        Field[] fields = data.getClass().getDeclaredFields();
        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();

        for (Field field : fields) {
            columns.append(field.getName()).append(",");
            placeholders.append("?,");
        }

        columns.setLength(columns.length() - 1);
        placeholders.setLength(placeholders.length() - 1);

        String query = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            int index = 1;

            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    Object value = field.get(data);

                    // Handle Enums (e.g., UserStatus) by mapping to their toString() value
                    if (value instanceof Enum) {
                        value = value.toString();
                    }

                    stmt.setObject(index++, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to access field value: " + field.getName(), e);
                }
            }

            stmt.executeUpdate();
        }
    }

    private void updateQuery(
            @NotNull K key,
            @NotNull String keyColumnName,
            @NotNull T data,
            @NotNull Connection conn,
            @NotNull String tableName
    ) throws SQLException, RuntimeException {


        Field[] fields = data.getClass().getDeclaredFields();
        StringBuilder setClause = new StringBuilder();

        for (Field field : fields) {
            if (!field.getName().equals(keyColumnName)) {
                setClause.append(field.getName()).append(" = ?,");
            }
        }
        setClause.setLength(setClause.length() - 1);

        String query = "UPDATE " + tableName + " SET " + setClause + " WHERE " + keyColumnName + " = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            int index = 1;

            // Устанавливаем параметры для полей объекта
            for (Field field : fields) {
                if (!field.getName().equals(keyColumnName)) {
                    field.setAccessible(true);
                    Object value = field.get(data);

                    // Обрабатываем Enum как строковые значения
                    if (value instanceof Enum) {
                        value = value.toString();
                    }

                    stmt.setObject(index++, value);
                }
            }

            // Устанавливаем значение ключа
            stmt.setObject(index, key);
            stmt.executeUpdate();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // =================================================================================== \\
    // THE CODE BELLOW IS PURPOSED FOR JSON OPERATOR AND HERE ONLY TO AN INHERITANCE NEEDS \\
    // =================================================================================== \\

    /**
     * Returns the content of a file.
     *
     * @param filePath path to destination file
     * @return returns the content of a file.
     * @throws JSONException if an error occurs during the operation
     */
    @Override
    public List<T> readFile(@NotNull String filePath) throws JSONException, IrregularAccessException {
        throw new IrregularAccessException(this.getClass().getName());
    }

    /**
     * Overwrites content of the provided file with new content
     *
     * @param filePath path to destination file
     * @param data     what to write in destination file
     * @throws JSONException if an error occurs during the operation
     */
    @Override
    public void writeToFile(@NotNull String filePath, @NotNull List<T> data) throws JSONException, IrregularAccessException {
        throw new IrregularAccessException(this.getClass().getName());
    }
}
