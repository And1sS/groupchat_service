package com.and1ss.group_chat_service.exceptions;

public class InternalServerException extends RuntimeException {
    private String msg = "";

    public InternalServerException() {}

    public InternalServerException(String msg) {
        this.msg = msg;
    }

    @Override
    public String getMessage() {
        return msg;
    }
}
