package com.yd.todo.global.exception.token;

public class InvalidRefreshTokenException extends RuntimeException {
	public InvalidRefreshTokenException(String msg) {
        super(msg);
    }
}
