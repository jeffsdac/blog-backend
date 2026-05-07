package br.com.jeffsdac.blog.blog.exception;

public class InvalidTokenException extends ApiException {
    public InvalidTokenException(String message) {
        super(message);
    }
}

