package resume.service.exceptions;

public class ConflictException extends RuntimeException{

    public ConflictException(String message) {
        super(message);
    }
}
