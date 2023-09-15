package org.ftclub.cabinet.user.repository;

import java.util.List;
import org.ftclub.cabinet.user.domain.LentExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LentExtensionRepository extends JpaRepository<LentExtension, Long> {

	@Query("SELECT le " +
			"FROM LentExtension le " +
			"WHERE le.user.userId =:userId " +
			"ORDER BY le.expiredAt ASC")
	List<LentExtension> findAllByUserId(@Param("userId") Long userId);

	@Query("SELECT le " +
			"FROM LentExtension le " +
			"WHERE le.expiredAt > CURRENT_TIMESTAMP " +
			"ORDER BY le.expiredAt ASC")
	List<LentExtension> findAllNotExpired();

	@Query("SELECT le " +
			"FROM LentExtension le " +
			"WHERE le.user.userId =:userId " +
			"AND le.expiredAt > CURRENT_TIMESTAMP " +
			"ORDER BY le.expiredAt ASC")
	List<LentExtension> findAllByUserIdNotExpired(Long userId);
}
