package br.com.jeffsdac.blog.blog.exception;

public class InvalidCredentialsException extends ApiException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}

