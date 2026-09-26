package com.onlinemarket.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MemberRequest {

	@NotBlank(message = "name is required")
	@Size(max = 100, message = "name must be at most 100 characters")
	private String name;

	@NotBlank(message = "email is required")
	@Email(message = "email must be valid")
	private String email;

	@NotBlank(message = "roomNumber is required")
	@Size(max = 20, message = "roomNumber must be at most 20 characters")
	private String roomNumber;
}
