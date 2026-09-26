package com.onlinemarket.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
		Map<String, String> fieldErrors = new HashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(error ->
			fieldErrors.put(error.getField(), error.getDefaultMessage()));

		ApiError body = ApiError.of(HttpStatus.BAD_REQUEST.value(), "Validation Failed",
			"One or more fields are invalid", fieldErrors);
		return ResponseEntity.badRequest().body(body);
	}

	@ExceptionHandler(MemberNotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(MemberNotFoundException ex) {
		ApiError body = ApiError.of(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}

	@ExceptionHandler(DuplicateEmailException.class)
	public ResponseEntity<ApiError> handleDuplicate(DuplicateEmailException ex) {
		ApiError body = ApiError.of(HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
	}
}
