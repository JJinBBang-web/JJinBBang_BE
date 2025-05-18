package JJinBBang.app.domain.user.controller;

import JJinBBang.app.domain.user.dto.request.LoginRequest;
import JJinBBang.app.domain.user.dto.response.LoginResponse;
import JJinBBang.app.domain.user.dto.response.ReissueAccessTokenResponse;
import JJinBBang.app.domain.user.dto.response.SignupRequiredResponse;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.service.OAuthService;
import JJinBBang.app.global.jwt.JwtUtils;
import JJinBBang.app.global.template.ResTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OAuthService oAuthService;
    private final JwtUtils jwtUtils;

    @PostMapping
    public ResTemplate<?> signIn(@RequestBody LoginRequest loginRequest){
        Users user = oAuthService.login(loginRequest.oauthProvider(), loginRequest.oauthCode());
        if(user.getUserId() == null){
            String signupToken = jwtUtils.generateSignupToken(user);

            SignupRequiredResponse signupRequiredResponse = SignupRequiredResponse.of(signupToken);
            return new ResTemplate<>(HttpStatus.OK, "약관동의가 필요합니다.", signupRequiredResponse);
        }
        else {
            String accessToken = jwtUtils.generateAccessToken(user);
            String refreshToken = jwtUtils.generateRefreshToken(user);

            LoginResponse loginResponse = LoginResponse.of(accessToken, refreshToken);
            return new ResTemplate<>(HttpStatus.OK, "로그인 성공", loginResponse);
        }
    }

    @PostMapping("/signup")
    public ResTemplate<LoginResponse> signUp(@AuthenticationPrincipal Users user){
        user = oAuthService.signup(user);
        String accessToken = jwtUtils.generateAccessToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);

        LoginResponse loginResponse = LoginResponse.of(accessToken, refreshToken);
        return new ResTemplate<>(HttpStatus.OK, "회원가입 성공", loginResponse);
    }

    @PutMapping("/tokenRefresh")
    public ResTemplate<ReissueAccessTokenResponse> reissueAccessToken(
            HttpServletRequest request,
            @AuthenticationPrincipal Users user
    ){
        String refreshToken = jwtUtils.extractToken(request);
        String accessToken = jwtUtils.reissueAccessToken(user, refreshToken);

        ReissueAccessTokenResponse response = ReissueAccessTokenResponse.of(accessToken);
        return new ResTemplate<>(HttpStatus.OK, "엑세스 토큰 재발급 성공", response);
    }
}
