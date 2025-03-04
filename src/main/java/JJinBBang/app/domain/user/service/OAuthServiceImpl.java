package JJinBBang.app.domain.user.service;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.service.login.LoginService;
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

    private final Map<String, LoginService> loginServiceMap = new HashMap<>();

    // 생성자 주입: @Autowired로 KakaoLoginService, GoogleLoginService, NaverLoginService 받아옴
    public OAuthServiceImpl(List<LoginService> loginServices) {
        // 구현체들을 순회하면서, providerName()을 키로 매핑
        for (LoginService service : loginServices) {
            loginServiceMap.put(service.getProviderName(), service);
        }
    }


    @Override
    public Users login(String oauthProvider, String oauthAccessToken) {
        LoginService loginService = loginServiceMap.get(oauthProvider);

        if (loginService == null) {
            // TODO : 지원하지 않는 소셜 타입 입니다.
            return null;
        }

        return loginService.login(oauthAccessToken);
    }

    @Override
    public Users signup(String oauthProvider, String oauthAccessToken) {
        LoginService loginService = loginServiceMap.get(oauthProvider);

        if (loginService == null) {
            // TODO : 지원하지 않는 소셜 타입 입니다.
            return null;
        }

        return loginService.signup(oauthAccessToken);
    }


}
