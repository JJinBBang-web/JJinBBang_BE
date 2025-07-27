package JJinBBang.app.domain.user.service.login;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.exception.GoogleAuthException;
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

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleLoginServiceImpl implements LoginService {

    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
    private String userInfoUri;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    private final UsersService usersService;

    @Override
    public String getProviderName() {
        return Provider.google.name();
    }

    @Override
    public String makeProviderId(String id) {
        return Provider.google.name() + "_" + id;
    }

    @Override
    public Users login(String oauthCode) {
        String accessToken = requestAccessToken(oauthCode);
        Map<String, Object> profile = requestUserInfo(accessToken);

        String googleId = (String) profile.get("sub");
        String providerId = makeProviderId(googleId);

        if (usersService.existsByProviderId(providerId)) {
            return usersService.findByProviderId(providerId);
        }

        // 신규 가입 flow
        return Users.builder()
                .provider(Provider.google)
                .providerId(providerId)
                .build();
    }

    private String requestAccessToken(String code) {
        RestTemplate rt = new RestTemplate();
        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String,String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map> resp = rt.postForEntity(tokenUri, request, Map.class);
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody()==null) {
            throw GoogleAuthException.accessTokenGenerateError();
        }
        return (String) resp.getBody().get("access_token");
    }

    private Map<String,Object> requestUserInfo(String accessToken) {
        RestTemplate rt = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> req = new HttpEntity<>(headers);

        ResponseEntity<Map> resp = rt.exchange(userInfoUri, HttpMethod.GET, req, Map.class);
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody()==null) {
            throw GoogleAuthException.userInfoFetchFailed();
        }
        return resp.getBody();
    }
}
