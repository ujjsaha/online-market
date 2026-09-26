package com.onlinemarket.service;

import com.onlinemarket.dto.MemberRequest;
import com.onlinemarket.dto.MemberResponse;
import com.onlinemarket.entity.Member;
import com.onlinemarket.exception.DuplicateEmailException;
import com.onlinemarket.exception.MemberNotFoundException;
import com.onlinemarket.repository.MemberRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MemberService {

	private final MemberRepository memberRepository;
	private final ModelMapper modelMapper;

	public MemberService(MemberRepository memberRepository, ModelMapper modelMapper) {
		this.memberRepository = memberRepository;
		this.modelMapper = modelMapper;
	}

	public List<MemberResponse> findAll() {
		return memberRepository.findAll().stream()
			.map(member -> modelMapper.map(member, MemberResponse.class))
			.toList();
	}

	public MemberResponse findById(Long id) {
		Member member = memberRepository.findById(id)
			.orElseThrow(() -> new MemberNotFoundException(id));
		return modelMapper.map(member, MemberResponse.class);
	}

	@Transactional
	public MemberResponse create(MemberRequest request) {
		if (memberRepository.existsByEmail(request.getEmail())) {
			throw new DuplicateEmailException(request.getEmail());
		}
		Member member = modelMapper.map(request, Member.class);
		Member saved = memberRepository.save(member);
		return modelMapper.map(saved, MemberResponse.class);
	}

	@Transactional
	public MemberResponse update(Long id, MemberRequest request) {
		Member member = memberRepository.findById(id)
			.orElseThrow(() -> new MemberNotFoundException(id));

		if (!member.getEmail().equals(request.getEmail()) && memberRepository.existsByEmail(request.getEmail())) {
			throw new DuplicateEmailException(request.getEmail());
		}

		modelMapper.map(request, member);
		return modelMapper.map(member, MemberResponse.class);
	}

	@Transactional
	public void delete(Long id) {
		if (!memberRepository.existsById(id)) {
			throw new MemberNotFoundException(id);
		}
		memberRepository.deleteById(id);
	}
}
