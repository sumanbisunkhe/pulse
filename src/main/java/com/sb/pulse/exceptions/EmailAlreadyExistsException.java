package com.sb.pulse.exceptions;

public class EmailAlreadyExistsException extends RuntimeException  {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
