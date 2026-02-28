package org.strelnikova.userservice.exception;
import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    private final UUID id;

    public UserNotFoundException(String message, UUID id) {
        super(message);
        this.id = id;
    }

    public UserNotFoundException(UUID id) {
        super("User not found with id: " + id);
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
