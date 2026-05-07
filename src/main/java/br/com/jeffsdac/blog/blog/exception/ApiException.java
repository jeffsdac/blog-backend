package br.com.jeffsdac.blog.blog.exception;

public abstract class ApiException extends RuntimeException {
    protected ApiException(String message) {
        super(message);
    }
}

