package com.onlinemarket.entity.stakeholder;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "t_stakeholders")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StakeholderEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Setter(AccessLevel.NONE)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String mobile;

	@Column(nullable = false, unique = true)
	private String email;

	private String address;

	@Column(nullable = false)
	private String password;

	@Transient
	private String confirmPassword;

	@Column(nullable = false)
	private String userType;

	private Long parentId;

	private String status;

	private LocalDateTime createdDate;

	private String createdBy;

	private LocalDateTime updatedDate;

	private String updatedBy;
}
