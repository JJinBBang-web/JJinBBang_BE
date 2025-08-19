package JJinBBang.app.domain.user.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import JJinBBang.app.domain.user.entity.Users;
import io.lettuce.core.dynamic.annotation.Param;

public interface UsersRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByProviderId(String providerId);

    @EntityGraph(attributePaths = {"university"})
    Optional<Users> findWithUniversityByProviderId(String providerId);

    Optional<Users> findByUserId(Long userId);

    @Query("SELECT u FROM Users u WHERE u.disabledAt IS NOT NULL AND u.disabledAt <= :deadline")
    List<Users> findAllDeletionDue(@Param("deadline") LocalDateTime deadline);

    boolean existsByProviderId(@Param("providerId") String providerId);
}
