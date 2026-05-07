package br.com.jeffsdac.blog.blog.exception;

public class ConflictException extends ApiException {
    public ConflictException(String message) {
        super(message);
    }
}

