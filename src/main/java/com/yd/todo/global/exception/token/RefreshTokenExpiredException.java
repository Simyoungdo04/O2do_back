package com.yd.todo.global.exception.token;

public class RefreshTokenExpiredException extends RuntimeException {
	public RefreshTokenExpiredException(String msg) {
		super(msg);
	}
}
