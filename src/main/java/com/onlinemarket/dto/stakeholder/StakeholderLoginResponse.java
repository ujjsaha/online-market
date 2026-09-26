package com.onlinemarket.dto.stakeholder;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StakeholderLoginResponse {

	private String token;
	private Stakeholder stakeholder;
}
