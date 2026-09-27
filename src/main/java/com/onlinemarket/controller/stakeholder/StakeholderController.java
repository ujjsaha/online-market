package com.onlinemarket.controller.stakeholder;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.onlinemarket.dto.Pagination;
import com.onlinemarket.dto.ResponseWrapper;
import com.onlinemarket.dto.stakeholder.Stakeholder;
import com.onlinemarket.dto.stakeholder.StakeholderLoginRequest;
import com.onlinemarket.dto.stakeholder.StakeholderLoginResponse;
import com.onlinemarket.exception.StakeholderNotFoundException;
import com.onlinemarket.exception.StakeholderValidationException;
import com.onlinemarket.service.stakeholder.StakeholderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/stakeholder")
public class StakeholderController {

	private static final String SUCCESS = "SUCCESS";
	private static final String FAILURE = "FAILURE";

	private final StakeholderService stakeholderService;

	public StakeholderController(StakeholderService stakeholderService) {
		this.stakeholderService = stakeholderService;
	}

	@PostMapping("/login")
	public ResponseWrapper<StakeholderLoginResponse> login(@Valid @RequestBody StakeholderLoginRequest request) {
		return execute(() -> stakeholderService.login(request.getEmail(), request.getPassword()), "Login successful");
	}

	@PostMapping("/saveOrUpdate")
	public ResponseWrapper<Stakeholder> saveOrUpdate(@Valid @RequestBody Stakeholder stakeholder) {
		return execute(() -> stakeholderService.saveOrUpdate(stakeholder), "Stakeholder saved successfully");
	}

	@GetMapping("/fetchById")
	public ResponseWrapper<Stakeholder> fetchById(@RequestParam Long id) {
		return execute(() -> stakeholderService.fetchById(id), "Stakeholder fetched successfully");
	}

	@PostMapping("/fetchAllStakeholders")
	public ResponseWrapper<List<Stakeholder>> fetchAllStakeholders(@RequestBody Pagination pagination) {
		return execute(() -> stakeholderService.fetchAllStakeholders(pagination), "Stakeholders fetched successfully");
	}

	@DeleteMapping("/delete")
	public ResponseWrapper<Void> delete(@RequestParam Long id) {
		return execute(() -> {
			stakeholderService.delete(id);
			return null;
		}, "Stakeholder deleted successfully");
	}

	private <T> ResponseWrapper<T> execute(java.util.function.Supplier<T> action, String successMessage) {
		ResponseWrapper<T> response = new ResponseWrapper<>();
		try {
			response.setData(action.get());
			response.setResponseCode(SUCCESS);
			response.setResponseMessage(successMessage);
		} catch (StakeholderNotFoundException | StakeholderValidationException ex) {
			response.setResponseCode(FAILURE);
			response.setResponseMessage(ex.getMessage());
		}
		return response;
	}
}
