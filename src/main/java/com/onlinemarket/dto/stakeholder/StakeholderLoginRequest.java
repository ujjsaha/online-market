package com.onlinemarket.dto.stakeholder;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StakeholderLoginRequest {

	@NotBlank(message = "email is required")
	@Email(message = "email must be valid")
	private String email;

	@NotBlank(message = "password is required")
	private String password;
}
