package JJinBBang.app.domain.user.service;

import JJinBBang.app.domain.user.dto.UserInfoResponseDto;
import org.springframework.stereotype.Service;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.exception.UserNotFoundException;
import JJinBBang.app.domain.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsersServiceImpl implements UsersService {

	private final UsersRepository usersRepository;

	@Override
	public boolean existsByProviderId(String providerId) {
        return usersRepository.findByProviderId(providerId).isPresent();
    }

	@Override
	public Users findByProviderId(String providerId) {
		try {
			return usersRepository.findByProviderId(providerId).orElseThrow(UserNotFoundException::notFound);
		} catch (UserNotFoundException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw UserNotFoundException.searchFailed();
		}
	}

	@Override
	public Users save(Users user) {
		return usersRepository.save(user);
	}

	@Override
	public UserInfoResponseDto getUserInfo(Users authUser) {
		Users user = usersRepository.findWithUniversityByProviderId(authUser.getProviderId())
				.orElseThrow(UserNotFoundException::notFound);
		return UserInfoResponseDto.of(user);
	}

	@Override
	public Users findWithUniversity(String providerId) {
		return usersRepository.findWithUniversityByProviderId(providerId)
				.orElseThrow(UserNotFoundException::notFound);
	}
}
