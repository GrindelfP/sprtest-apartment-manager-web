package to.grindelf.sprtest.exceptions;

public class UnimplementedDataAccessException extends Throwable {

    public UnimplementedDataAccessException(String message) {
        super(message);
    }

    public UnimplementedDataAccessException() {
        super("This data access method is not implemented!");
    }
}
