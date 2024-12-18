package to.grindelf.sprtest.exceptions;

public class IllegalExecutionModeException extends Throwable {

    public IllegalExecutionModeException() {
        System.err.println("This execution mode is not supported!");
    }
}
