package com.onlinemarket.exception;

public class DuplicateEmailException extends RuntimeException {

	public DuplicateEmailException(String email) {
		super("A member with email '" + email + "' already exists");
	}
}
