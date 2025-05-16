package JJinBBang.app.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import JJinBBang.app.domain.user.entity.Users;

public interface UsersRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByProviderId(String providerId);
}