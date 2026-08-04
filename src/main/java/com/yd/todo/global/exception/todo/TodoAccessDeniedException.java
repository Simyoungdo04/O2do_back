package com.yd.todo.global.exception.todo;

public class TodoAccessDeniedException extends RuntimeException {
    public TodoAccessDeniedException(String msg) {
        super(msg);
    }
}