package com.yd.todo.global.exception.user;

public class DuplicateLoginIdException extends RuntimeException {
    public DuplicateLoginIdException(String msg) {
        super(msg);
    }
}
