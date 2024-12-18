package to.grindelf.sprtest.exceptions;

public class UserAlreadyExistsException extends Exception {

    public UserAlreadyExistsException() {
        super("This user already exists! Try another user name.");
    }

}
