package JJinBBang.app.domain.user.service;

import JJinBBang.app.domain.user.entity.Users;

public interface UsersService {

	boolean existsByProviderId(String providerId);

	Users findByProviderId(String providerId);

	Users save(Users user);
}
