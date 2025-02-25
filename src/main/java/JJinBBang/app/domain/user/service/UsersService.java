package JJinBBang.app.domain.user.service;

import JJinBBang.app.domain.user.entity.Users;

public interface UsersService {
	Users findByProviderId(String providerId);

	Users save(Users user);
}
