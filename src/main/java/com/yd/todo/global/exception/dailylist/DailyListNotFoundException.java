package com.yd.todo.global.exception.dailylist;

public class DailyListNotFoundException extends RuntimeException {
    public DailyListNotFoundException(String msg) {
        super(msg);
    }
}