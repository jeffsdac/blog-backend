package br.com.jeffsdac.blog.blog.exception;

public class ValidationException extends ApiException {
    public ValidationException(String message) {
        super(message);
    }
}

