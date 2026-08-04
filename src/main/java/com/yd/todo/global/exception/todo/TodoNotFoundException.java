package com.yd.todo.global.exception.todo;

public class TodoNotFoundException extends RuntimeException {
    public TodoNotFoundException(String msg) {
        super(msg);
    }
}