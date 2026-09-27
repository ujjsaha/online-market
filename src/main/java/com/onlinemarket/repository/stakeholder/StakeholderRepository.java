package com.onlinemarket.repository.stakeholder;

import com.onlinemarket.entity.stakeholder.StakeholderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StakeholderRepository extends JpaRepository<StakeholderEntity, Long> {

	Optional<StakeholderEntity> findByEmail(String email);

	boolean existsByEmail(String email);

	@Query("""
		select s from StakeholderEntity s
		where lower(s.name) like lower(concat('%', :searchBy, '%'))
			or lower(s.email) like lower(concat('%', :searchBy, '%'))
		""")
	Page<StakeholderEntity> search(@Param("searchBy") String searchBy, Pageable pageable);
}
