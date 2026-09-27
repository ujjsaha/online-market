package com.onlinemarket.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlinemarket.dto.stakeholder.Stakeholder;
import com.onlinemarket.service.stakeholder.StakeholderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class JwtAuthenticationIntegrationTest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private StakeholderService stakeholderService;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void loginIssuesTokenThatUnlocksProtectedEndpoints() throws Exception {
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();

		Stakeholder dto = new Stakeholder();
		dto.setName("Jane Doe");
		dto.setMobile("9999999999");
		dto.setEmail("jane.doe@example.com");
		dto.setPassword("Secret123");
		dto.setConfirmPassword("Secret123");
		dto.setUserType("ADMIN");
		Long stakeholderId = stakeholderService.saveOrUpdate(dto).getId();

		mockMvc.perform(get("/stakeholder/fetchById").param("id", String.valueOf(stakeholderId)))
			.andExpect(status().isUnauthorized());

		String loginBody = """
			{"email":"jane.doe@example.com","password":"Secret123"}
			""";

		String responseJson = mockMvc.perform(post("/stakeholder/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginBody))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		JsonNode root = objectMapper.readTree(responseJson);
		String token = root.path("data").path("token").asText();
		org.assertj.core.api.Assertions.assertThat(token).isNotBlank();

		mockMvc.perform(get("/stakeholder/fetchById").param("id", String.valueOf(stakeholderId))
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isOk());

		mockMvc.perform(get("/actuator/health"))
			.andExpect(status().isOk());
	}
}
