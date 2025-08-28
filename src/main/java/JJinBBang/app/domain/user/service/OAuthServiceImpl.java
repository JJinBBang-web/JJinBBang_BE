package JJinBBang.app.domain.user.service;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.exception.AuthNotFoundException;
import JJinBBang.app.domain.user.exception.UserAuthException;
import JJinBBang.app.domain.user.service.login.LoginService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthServiceImpl implements OAuthService {

    private final List<LoginService> loginServices;
    private final Map<String, LoginService> loginServiceMap = new HashMap<>();

    private final UsersService usersService;

    // KakaoLoginService, GoogleLoginService, NaverLoginService 받아오기
    @PostConstruct
    public void init() {
        // 구현체들을 순회하면서, providerName()을 키로 매핑
        for (LoginService service : loginServices) {
            loginServiceMap.put(service.getProviderName(), service);
        }
    }


    @Override
    public Users login(String oauthProvider, String oauthCode, String redirectUri) {
        LoginService loginService = loginServiceMap.get(oauthProvider);

        if (loginService == null) {
            // 지원하지 않는 소셜 타입 입니다.
            throw AuthNotFoundException.socialProviderNotFound();
        }

        return loginService.login(oauthCode, redirectUri);
    }

    @Override
    public Users signup(Users user) {

        if(usersService.existsByProviderId(user.getProviderId())){
            // 이미 가입된 회원입니다.
            throw UserAuthException.alreadyExists();
        }

        return usersService.save(user);
    }
}
