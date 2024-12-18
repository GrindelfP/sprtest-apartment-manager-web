package to.grindelf.sprtest.exceptions;

public class NoSuchUserException extends Exception {

    public NoSuchUserException() {
        System.err.println("No such authorized user!");
    }
}
