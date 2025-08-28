package JJinBBang.app.domain.user.service.login;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.exception.KakaoAuthException;
import JJinBBang.app.domain.user.exception.NaverAuthException;
import JJinBBang.app.domain.user.service.UsersService;
import JJinBBang.app.global.common.enums.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverLoginServiceImpl implements LoginService {

    @Value("${spring.security.oauth2.client.provider.naver.token-uri}")
    private String tokenUrl;

    @Value("${spring.security.oauth2.client.provider.naver.user-info-uri}")
    private String userInfoUrl;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String clientSecret;

    @Value("${security.oauth.allowed-redirect-uri.local.naver}")
    private String localRedirectUri;

    @Value("${security.oauth.allowed-redirect-uri.prod.naver}")
    private String prodRedirectUri;

    private final UsersService usersService;

    @Override
    public String getProviderName() {
        return Provider.naver.name();
    }

    @Override
    public String makeProviderId(String id) {
        return Provider.naver.name() + "_" + id;
    }

    @Override
    public Users login(String oauthCode, String redirectUri) {
        // redirectUri 검증
        List<String> allowedRedirectUris = List.of(localRedirectUri, prodRedirectUri);
        if(!allowedRedirectUris.contains(redirectUri)){
            throw NaverAuthException.notAllowedRedirectUri();
        }

        // 1) 액세스 토큰 발급
        String accessToken = getAccessToken(oauthCode, redirectUri);

        // 2) 유저 정보 조회
        Map<String, Object> naverUserInfo = getUserInfo(accessToken);
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) naverUserInfo.get("response");
        String naverId = (String) response.get("id");

        // 3) providerId 조합
        String providerId = makeProviderId(naverId);

        // 4) DB에 있으면 가져오고, 없으면 회원가입 대기용 DTO 리턴
        if (usersService.existsByProviderId(providerId)) {
            return usersService.findByProviderId(providerId);
        }

        return Users.builder()
                .provider(Provider.naver)
                .providerId(providerId)
                .build();
    }

    private String getAccessToken(String code, String redirectUri) {
        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("redirect_uri", redirectUri);
            params.add("code", code);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            RestTemplate rt = new RestTemplate();
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            ResponseEntity<Map> response = rt.postForEntity(tokenUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("access_token");
            }
            throw NaverAuthException.accessTokenGenerateError();
        } catch (Exception e) {
            log.error("네이버 AccessToken 발급 실패", e);
            throw NaverAuthException.accessTokenGenerateError();
        }
    }

    private Map<String, Object> getUserInfo(String accessToken) {
        try {
            RestTemplate rt = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<Map> response = rt.exchange(userInfoUrl, HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            throw NaverAuthException.userInfoFetchFailed();
        } catch (Exception e) {
            log.error("네이버 유저 정보 조회 실패", e);
            throw NaverAuthException.userInfoFetchFailed();
        }
    }
}
