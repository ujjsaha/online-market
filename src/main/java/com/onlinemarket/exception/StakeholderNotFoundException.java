package com.onlinemarket.exception;

public class StakeholderNotFoundException extends RuntimeException {

	public StakeholderNotFoundException(Long id) {
		super("Stakeholder not found with id: " + id);
	}

	public StakeholderNotFoundException(String message) {
		super(message);
	}
}
