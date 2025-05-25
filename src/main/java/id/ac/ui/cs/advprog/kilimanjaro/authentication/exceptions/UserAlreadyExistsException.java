package id.ac.ui.cs.advprog.kilimanjaro.authentication.exceptions;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
