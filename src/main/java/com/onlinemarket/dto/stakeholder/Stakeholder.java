package com.onlinemarket.dto.stakeholder;

import lombok.Data;

@Data
public class Stakeholder {

	private Long id;
	private String name;
	private String mobile;
	private String email;
	private String address;
	private String password;
	private String confirmPassword;
	private String userType;
	private Long parentId;
	private String status;
}
