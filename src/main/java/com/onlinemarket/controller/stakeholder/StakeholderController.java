package com.onlinemarket.controller.stakeholder;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
	public ResponseEntity<ResponseWrapper<StakeholderLoginResponse>> login(@Valid @RequestBody StakeholderLoginRequest request) {
		return execute(() -> stakeholderService.login(request.getEmail(), request.getPassword()), "Login successful",
			HttpStatus.UNAUTHORIZED);
	}

	@PostMapping("/saveOrUpdate")
	public ResponseEntity<ResponseWrapper<Stakeholder>> saveOrUpdate(@Valid @RequestBody Stakeholder stakeholder) {
		return execute(() -> stakeholderService.saveOrUpdate(stakeholder), "Stakeholder saved successfully");
	}

	@GetMapping("/fetchById")
	public ResponseEntity<ResponseWrapper<Stakeholder>> fetchById(@RequestParam("id") Long id) {
		return execute(() -> stakeholderService.fetchById(id), "Stakeholder fetched successfully");
	}

	@PostMapping("/fetchAllStakeholders")
	public ResponseEntity<ResponseWrapper<List<Stakeholder>>> fetchAllStakeholders(@RequestBody Pagination pagination) {
		return execute(() -> stakeholderService.fetchAllStakeholders(pagination), "Stakeholders fetched successfully");
	}

	@DeleteMapping("/delete")
	public ResponseEntity<ResponseWrapper<Void>> delete(@RequestParam("id") Long id) {
		return execute(() -> {
			stakeholderService.delete(id);
			return null;
		}, "Stakeholder deleted successfully");
	}

	private <T> ResponseEntity<ResponseWrapper<T>> execute(Supplier<T> action, String successMessage) {
		return execute(action, successMessage, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Runs the action and wraps the outcome. HTTP 200 is used only for success;
	 * a missing stakeholder maps to 404 and a validation failure to {@code validationStatus}.
	 */
	private <T> ResponseEntity<ResponseWrapper<T>> execute(Supplier<T> action, String successMessage,
			HttpStatus validationStatus) {
		ResponseWrapper<T> response = new ResponseWrapper<>();
		try {
			response.setData(action.get());
			response.setResponseCode(SUCCESS);
			response.setResponseMessage(successMessage);
			return ResponseEntity.ok(response);
		} catch (StakeholderNotFoundException ex) {
			return failure(response, ex, HttpStatus.NOT_FOUND);
		} catch (StakeholderValidationException ex) {
			return failure(response, ex, validationStatus);
		}
	}

	private <T> ResponseEntity<ResponseWrapper<T>> failure(ResponseWrapper<T> response, RuntimeException ex,
			HttpStatus status) {
		response.setResponseCode(FAILURE);
		response.setResponseMessage(ex.getMessage());
		return ResponseEntity.status(status).body(response);
	}
}
