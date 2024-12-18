package to.grindelf.sprtest.utils.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import to.grindelf.sprtest.annonations.JSONPurposed;
import to.grindelf.sprtest.exceptions.IrregularAccessException;
import to.grindelf.sprtest.exceptions.JSONException;
import to.grindelf.sprtest.exceptions.NoSuchUserException;
import to.grindelf.sprtest.exceptions.UserAlreadyExistsException;
import to.grindelf.sprtest.utils.ConstantValues.DatabaseTableNames;
import to.grindelf.sprtest.utils.DataOperator;
import to.grindelf.sprtest.utils.database.RowMapper;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class JsonOperator<T, K> implements DataOperator<T, K> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TypeReference<List<T>> typeReference;

    /**
     * Constructor that initializes the type reference.
     *
     * @param typeReference type reference to initialize with
     */
    public JsonOperator(TypeReference<List<T>> typeReference) {
        this.typeReference = typeReference;
    }

    /**
     * Returns the content of a file.
     *
     * @param filePath path to destination file
     * @return returns the content of a file.
     * @throws JSONException if an error occurs during the operation
     */
    @Override
    public List<T> readFile(@NotNull String filePath) throws JSONException, IrregularAccessException {
        try {
            return objectMapper.readValue(new File(filePath), typeReference);
        } catch (IOException e) {
            e.printStackTrace();
            throw new JSONException("Error occurred while reading from file");
        }
    }

    /**
     * Overwrites content of the provided JSON file with new content
     *
     * @param filePath path to destination JSON file
     * @param data     what to write in destination JSON file
     */
    @Override
    @JSONPurposed
    public void writeToFile(@NotNull String filePath, @NotNull List<T> data) throws JSONException, IrregularAccessException {
        try {
            objectMapper.writeValue(new File(filePath), data);
        } catch (IOException e) {
            e.printStackTrace();
            throw new JSONException("Error occurred while writing to file");
        }
    }

    // ================================================================================== \\
    // THE CODE BELLOW IS PURPOSED FOR SQL OPERATOR AND HERE ONLY TO AN INHERITANCE NEEDS \\
    // ================================================================================== \\

    /**
     * Returns the object by the provided key from the database file.
     *
     * @param key           key to get the object by
     * @param keyColumnName
     * @param filePath      path to the database file
     * @param tableName     name of the table to get data from
     * @return returns the object by the provided key from the database file
     * @throws SQLException if an error occurs during the operation
     */
    @Override
    public T getByKey(
            @NotNull K key,
            @NotNull String keyColumnName,
            @NotNull String filePath,
            @NotNull DatabaseTableNames tableName,
            @NotNull RowMapper<T> mapper
    ) throws SQLException, IrregularAccessException, NoSuchUserException {
        throw new IrregularAccessException(this.getClass().getName());
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
    public @NotNull List<T> getAll(
            @NotNull String filePath,
            @NotNull RowMapper<T> mapper,
            @NotNull DatabaseTableNames tableName
    ) throws SQLException, IrregularAccessException {
        throw new IrregularAccessException(this.getClass().getName());
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
    public void post(
            @NotNull T data,
            @NotNull String filePath,
            @NotNull DatabaseTableNames tableName,
            @NotNull String keyColumnName) throws SQLException, IrregularAccessException, UserAlreadyExistsException {
        throw new IrregularAccessException(this.getClass().getName());
    }

    /**
     * Updates the data in the database file.
     *
     * @param key       key to update the object by
     * @param filePath  path to the database file
     * @param tableName name of the table to update data in
     * @throws SQLException if an error occurs during the operation
     */
    @Override
    public void update(
            @NotNull K key,
            @NotNull String keyColumnName,
            @NotNull T data,
            @NotNull String filePath,
            @NotNull DatabaseTableNames tableName
    ) throws SQLException, IrregularAccessException, NoSuchUserException {
        throw new IrregularAccessException(this.getClass().getName());
    }

    /**
     * Deletes the data from the database file.
     *
     * @param key           key to delete the object by
     * @param keyColumnName
     * @param filePath      path to the database file
     * @param tableName     name of the table to delete data from
     * @throws SQLException if an error occurs during the operation
     */
    @Override
    public void delete(
            @NotNull K key,
            @NotNull String keyColumnName, @NotNull String filePath,
            @NotNull DatabaseTableNames tableName
    ) throws SQLException, IrregularAccessException, NoSuchUserException {
        throw new IrregularAccessException(this.getClass().getName());
    }
}
