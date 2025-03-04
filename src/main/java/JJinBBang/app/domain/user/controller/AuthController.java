package JJinBBang.app.domain.user.controller;

import JJinBBang.app.domain.user.dto.request.LoginRequest;
import JJinBBang.app.domain.user.dto.request.SignupRequest;
import JJinBBang.app.domain.user.dto.response.LoginResponse;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.service.OAuthService;
import JJinBBang.app.global.jwt.JwtUtils;
import JJinBBang.app.global.template.ResTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OAuthService oAuthService;
    private final JwtUtils jwtUtils;

    @PostMapping
    public ResTemplate<LoginResponse> signIn(@RequestBody LoginRequest loginRequest){
        Users user = oAuthService.login(loginRequest.oauthProvider(), loginRequest.oauthAccessToken());
        String accessToken = jwtUtils.generateAccessToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);

        LoginResponse loginResponse = LoginResponse.of(accessToken, refreshToken);
        return new ResTemplate<>(HttpStatus.OK, "로그인 성공", loginResponse);
    }

    @PostMapping("/signup")
    public ResTemplate<LoginResponse> signUp(@RequestBody SignupRequest signupRequest){
        Users user = oAuthService.signup(signupRequest.oauthProvider(), signupRequest.oauthAccessToken());
        String accessToken = jwtUtils.generateAccessToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);

        LoginResponse loginResponse = LoginResponse.of(accessToken, refreshToken);
        return new ResTemplate<>(HttpStatus.OK, "회원가입 성공", loginResponse);
    }
}
