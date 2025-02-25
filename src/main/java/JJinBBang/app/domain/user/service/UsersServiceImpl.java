package JJinBBang.app.domain.user.service;

import org.springframework.stereotype.Service;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

	private final UsersRepository usersRepository;

	@Override
	public Users findByProviderId(String providerId) {
		// 나중에 예외처리 방식 바꿔야 함
		return usersRepository.findByProviderId(providerId).orElse(null);
	}

	@Override
	public Users save(Users user) {
		return usersRepository.save(user);
	}
}
