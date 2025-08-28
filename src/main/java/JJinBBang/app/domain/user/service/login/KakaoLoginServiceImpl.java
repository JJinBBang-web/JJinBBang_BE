package JJinBBang.app.domain.user.service.login;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.exception.KakaoAuthException;
import JJinBBang.app.domain.user.service.UsersService;
import JJinBBang.app.global.common.enums.Provider;
import jakarta.annotation.PostConstruct;
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
public class KakaoLoginServiceImpl implements LoginService{

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String userInfoUrl;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String tokenUrl;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;

    @Value("${security.oauth.allowed-redirect-uri.local.kakao}")
    private String localRedirectUri;

    @Value("${security.oauth.allowed-redirect-uri.prod.kakao}")
    private String prodRedirectUri;


    private final UsersService usersService;


    @Override
    public String getProviderName() {
        return Provider.kakao.name();
    }

    @Override
    public String makeProviderId(String id) {
        return Provider.kakao.name() + "_" + id;
    }

    @Override
    public Users login(String oauthCode, String redirectUri) {
        // redirectUri 검증
        List<String> allowedRedirectUris = List.of(localRedirectUri, prodRedirectUri);
        if(redirectUri == null || !allowedRedirectUris.contains(redirectUri)){
            throw KakaoAuthException.notAllowedRedirectUri();
        }

        String oauthAccessToken = getAccessToken(oauthCode, redirectUri);

        Map<String, Object> kakaoUserInfo = getKakaoUserInfo(oauthAccessToken);

        // 카카오에서 내려주는 'id' (고유 회원번호), Long/Integer 형태 가능
        long kakaoId = ((Number) kakaoUserInfo.get("id")).longValue();

        String providerId = makeProviderId(Long.toString(kakaoId));

        if(usersService.existsByProviderId(providerId)){
            // 이미 존재하는 계정
            return usersService.findByProviderId(providerId);
        }

        // DB에 저장하지 않은 유저 객체 반환 (약관동의 필요)
        return Users.builder()
                .provider(Provider.kakao)
                .providerId(providerId)
                .build();
    }


    private String getAccessToken(String code, String redirectUri) {
        try {
            // 요청 파라미터
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", clientId);  // 실제론 yml나 env에서 주입
            params.add("redirect_uri", redirectUri); // 카카오 개발자센터에 등록된 Redirect URI
            params.add("client_secret", clientSecret); // 카카오 개발자센터에 등록된 Client Secret
            params.add("code", code);

            // 헤더
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 카카오 서버에 POST
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<MultiValueMap<String, String>> request =
                    new HttpEntity<>(params, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map body = response.getBody();
                // "access_token" 키로 토큰값이 들어옴
                return (String) body.get("access_token");
            }

            // 토큰 발급 실패
            throw KakaoAuthException.accessTokenGenerateError();
        } catch (Exception e) {
            throw KakaoAuthException.accessTokenGenerateError();
        }
    }


    private Map<String, Object> getKakaoUserInfo(String oauthAccessToken) {
        RestTemplate restTemplate = new RestTemplate();
        // 헤더에 Authorization: Bearer {accessToken}
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(oauthAccessToken);
        // 보통 application/x-www-form-urlencoded 로 POST
        // (GET으로도 가능하지만, 카카오 문서에선 POST 권장)
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        // POST 요청
        ResponseEntity<Map> response = restTemplate.postForEntity(
                userInfoUrl, requestEntity, Map.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw KakaoAuthException.userInfoFetchFailed();
        }
    }
}
