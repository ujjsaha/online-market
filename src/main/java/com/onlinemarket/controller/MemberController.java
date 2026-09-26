package com.onlinemarket.controller;

import com.onlinemarket.dto.MemberRequest;
import com.onlinemarket.dto.MemberResponse;
import com.onlinemarket.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

	private final MemberService memberService;

	public MemberController(MemberService memberService) {
		this.memberService = memberService;
	}

	@GetMapping
	public List<MemberResponse> findAll() {
		return memberService.findAll();
	}

	@GetMapping("/{id}")
	public MemberResponse findById(@PathVariable Long id) {
		return memberService.findById(id);
	}

	@PostMapping
	public ResponseEntity<MemberResponse> create(@Valid @RequestBody MemberRequest request) {
		MemberResponse created = memberService.create(request);
		return ResponseEntity.created(URI.create("/api/members/" + created.getId())).body(created);
	}

	@PutMapping("/{id}")
	public MemberResponse update(@PathVariable Long id, @Valid @RequestBody MemberRequest request) {
		return memberService.update(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		memberService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
