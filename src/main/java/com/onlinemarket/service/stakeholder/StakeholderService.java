package com.onlinemarket.service.stakeholder;

import com.onlinemarket.dto.Pagination;
import com.onlinemarket.dto.stakeholder.Stakeholder;
import com.onlinemarket.dto.stakeholder.StakeholderLoginResponse;
import com.onlinemarket.entity.stakeholder.StakeholderEntity;
import com.onlinemarket.exception.StakeholderNotFoundException;
import com.onlinemarket.exception.StakeholderValidationException;
import com.onlinemarket.mapper.stakeholder.StakeholderMapper;
import com.onlinemarket.repository.stakeholder.StakeholderRepository;
import com.onlinemarket.security.JwtService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class StakeholderService {

	private static final Set<String> SORTABLE_FIELDS =
		Set.of("id", "name", "email", "mobile", "status", "userType", "createdDate");
	private static final String DEFAULT_SORT_FIELD = "id";
	private static final int DEFAULT_PAGE_SIZE = 10;

	private final StakeholderRepository stakeholderRepository;
	private final StakeholderMapper stakeholderMapper;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public StakeholderService(StakeholderRepository stakeholderRepository, StakeholderMapper stakeholderMapper,
			PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.stakeholderRepository = stakeholderRepository;
		this.stakeholderMapper = stakeholderMapper;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	public StakeholderLoginResponse login(String email, String password) {
		StakeholderEntity entity = stakeholderRepository.findByEmail(email)
			.orElseThrow(() -> new StakeholderValidationException("Invalid email or password"));

		if (!passwordEncoder.matches(password, entity.getPassword())) {
			throw new StakeholderValidationException("Invalid email or password");
		}

		String token = jwtService.generateToken(entity.getEmail(), Map.of("roles", List.of(roleOf(entity))));
		return new StakeholderLoginResponse(token, stakeholderMapper.toDto(entity));
	}

	private String roleOf(StakeholderEntity entity) {
		String userType = StringUtils.hasText(entity.getUserType()) ? entity.getUserType() : "USER";
		return "ROLE_" + userType.toUpperCase(Locale.ROOT);
	}

	@Transactional
	public Stakeholder saveOrUpdate(Stakeholder dto) {
		return dto.getId() == null ? create(dto) : update(dto);
	}

	private Stakeholder create(Stakeholder dto) {
		if (!StringUtils.hasText(dto.getPassword()) || !dto.getPassword().equals(dto.getConfirmPassword())) {
			throw new StakeholderValidationException("password and confirmPassword must be provided and match");
		}
		if (stakeholderRepository.existsByEmail(dto.getEmail())) {
			throw new StakeholderValidationException("A stakeholder with email '" + dto.getEmail() + "' already exists");
		}

		StakeholderEntity entity = stakeholderMapper.toEntity(dto);
		entity.setPassword(passwordEncoder.encode(dto.getPassword()));
		entity.setCreatedDate(LocalDateTime.now());
		entity.setCreatedBy(currentActor());

		StakeholderEntity saved = stakeholderRepository.save(entity);
		return stakeholderMapper.toDto(saved);
	}

	private Stakeholder update(Stakeholder dto) {
		StakeholderEntity entity = stakeholderRepository.findById(dto.getId())
			.orElseThrow(() -> new StakeholderNotFoundException(dto.getId()));

		if (!entity.getEmail().equals(dto.getEmail()) && stakeholderRepository.existsByEmail(dto.getEmail())) {
			throw new StakeholderValidationException("A stakeholder with email '" + dto.getEmail() + "' already exists");
		}

		String existingPassword = entity.getPassword();
		stakeholderMapper.updateEntity(dto, entity);

		if (StringUtils.hasText(dto.getPassword())) {
			if (!dto.getPassword().equals(dto.getConfirmPassword())) {
				throw new StakeholderValidationException("password and confirmPassword must match");
			}
			entity.setPassword(passwordEncoder.encode(dto.getPassword()));
		} else {
			entity.setPassword(existingPassword);
		}

		entity.setUpdatedDate(LocalDateTime.now());
		entity.setUpdatedBy(currentActor());

		StakeholderEntity saved = stakeholderRepository.save(entity);
		return stakeholderMapper.toDto(saved);
	}

	public Stakeholder fetchById(Long id) {
		StakeholderEntity entity = stakeholderRepository.findById(id)
			.orElseThrow(() -> new StakeholderNotFoundException(id));
		return stakeholderMapper.toDto(entity);
	}

	public List<Stakeholder> fetchAllStakeholders(Pagination pagination) {
		Pageable pageable = toPageable(pagination);
		// Never pass null: PostgreSQL can't infer the type of a null bind parameter, and "" matches every row.
		String searchBy = StringUtils.hasText(pagination.getSearchBy()) ? pagination.getSearchBy().trim() : "";
		Page<StakeholderEntity> page = stakeholderRepository.search(searchBy, pageable);
		pagination.setPages(page.getTotalPages());
		return page.getContent().stream()
			.map(stakeholderMapper::toDto)
			.toList();
	}

	@Transactional
	public void delete(Long id) {
		if (!stakeholderRepository.existsById(id)) {
			throw new StakeholderNotFoundException(id);
		}
		stakeholderRepository.deleteById(id);
	}

	private Pageable toPageable(Pagination pagination) {
		String sortField = SORTABLE_FIELDS.contains(pagination.getSortBy()) ? pagination.getSortBy() : DEFAULT_SORT_FIELD;
		Sort.Direction direction = "DESC".equalsIgnoreCase(pagination.getOrderBy()) ? Sort.Direction.DESC : Sort.Direction.ASC;
		int pageSize = pagination.getPageSize() > 0 ? pagination.getPageSize() : DEFAULT_PAGE_SIZE;
		int pageNumber = Math.max(pagination.getPageNumber() - 1, 0);
		return PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortField));
	}

	private String currentActor() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication != null ? authentication.getName() : "system";
	}
}
