package JJinBBang.app.domain.user.service;

import JJinBBang.app.domain.user.dto.UserInfoResponseDto;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserInfoService {
    public UserInfoResponseDto getUserInfo(Users user) {
        if (user == null) {
            throw UserNotFoundException.notFound(); // 404
        }
        return UserInfoResponseDto.of(user);
    }
}
