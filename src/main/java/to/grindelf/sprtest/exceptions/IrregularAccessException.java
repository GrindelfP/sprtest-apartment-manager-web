package to.grindelf.sprtest.exceptions;

/**
 * Exception for irregular access.
 * Thrown if a function is called from an implementation that is not supposed to call it.
 */
public class IrregularAccessException extends RuntimeException {

    /**
     * Constructor for IrregularAccessException.
     * @param currentImplementation String with the name of the current implementation.
     */
    public IrregularAccessException(String currentImplementation) {
        super("This function is not callable from " + currentImplementation + " implementation!");
    }
}
