package com.and1ss.group_chat_service.exceptions;

public class InvalidLoginCredentialsException extends RuntimeException {
    @Override
    public String getMessage() {
        return "Incorrect login or password";
    }
}
