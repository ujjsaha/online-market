package com.onlinemarket.mapper.stakeholder;

import com.onlinemarket.dto.stakeholder.Stakeholder;
import com.onlinemarket.entity.stakeholder.StakeholderEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class StakeholderMapper {

	private final ModelMapper modelMapper;

	public StakeholderMapper(ModelMapper modelMapper) {
		this.modelMapper = modelMapper;
	}

	public StakeholderEntity toEntity(Stakeholder dto) {
		return modelMapper.map(dto, StakeholderEntity.class);
	}

	public Stakeholder toDto(StakeholderEntity entity) {
		Stakeholder dto = modelMapper.map(entity, Stakeholder.class);
		dto.setPassword(null);
		dto.setConfirmPassword(null);
		return dto;
	}

	public void updateEntity(Stakeholder dto, StakeholderEntity entity) {
		modelMapper.map(dto, entity);
	}
}
