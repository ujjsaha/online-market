package com.onlinemarket.dto;

import lombok.Data;

@Data
public class ResponseWrapper<T> {

	private String responseCode;
	private String responseMessage;
	private T data;
}
