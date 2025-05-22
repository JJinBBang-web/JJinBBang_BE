package JJinBBang.app.domain.user.controller;

import JJinBBang.app.domain.user.dto.request.IssueEmailCodeRequest;
import JJinBBang.app.domain.user.dto.request.LoginRequest;
import JJinBBang.app.domain.user.dto.request.SignupRequest;
import JJinBBang.app.domain.user.dto.request.VerifyEmailCodeRequest;
import JJinBBang.app.domain.user.dto.response.LoginResponse;
import JJinBBang.app.domain.user.dto.response.SignupRequiredResponse;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.service.OAuthService;
import JJinBBang.app.domain.user.service.UsersService;
import JJinBBang.app.global.jwt.JwtUtils;
import JJinBBang.app.global.mail.service.MailAuthService;
import JJinBBang.app.global.template.ResTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsersService usersService;
    private final OAuthService oAuthService;
    private final JwtUtils jwtUtils;
    private final MailAuthService mailAuthService;

    @PostMapping
    public ResTemplate<?> signIn(@RequestBody LoginRequest loginRequest) {
        // 소셜 로그인 API

        // 전달받은 provider에 따라 매칭되는 LoginService 인터페이스 구현체를 실행하여 소셜 로그인 진행
        Users user = oAuthService.login(loginRequest.oauthProvider(), loginRequest.oauthCode());

        if (user.getUserId() == null) {
            // 약관 동의가 필요한 경우
            // user 객체는 DB에 저장되지 않은 상태. (provider, providerId 만 존재)
            // -> 약관동의를 위한 임시 토큰 발급 (토큰에 provider, providerId 포함)
            String signupToken = jwtUtils.generateSignupToken(user);

            SignupRequiredResponse signupRequiredResponse = SignupRequiredResponse.of(signupToken);
            return new ResTemplate<>(HttpStatus.PRECONDITION_REQUIRED, "약관동의가 필요합니다.", signupRequiredResponse);
        } else {
            // 약관 동의가 완료된 유저의 경우
            // user 객체는 DB에 저장된 상태 -> 엑세스 토큰, 리프레시 토큰 발급
            String accessToken = jwtUtils.generateAccessToken(user);
            String refreshToken = jwtUtils.generateRefreshToken(user);

            LoginResponse loginResponse = LoginResponse.of(accessToken, refreshToken);
            return new ResTemplate<>(HttpStatus.OK, "로그인 성공", loginResponse);
        }
    }

    @PostMapping("/signup")
    public ResTemplate<LoginResponse> signUp(@AuthenticationPrincipal Users user) {
        // 로그인 한 유저에 대해 약관 동의를 수행하는 API 입니다.

        user = oAuthService.signup(user);
        String accessToken = jwtUtils.generateAccessToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);

        LoginResponse loginResponse = LoginResponse.of(accessToken, refreshToken);
        return new ResTemplate<>(HttpStatus.OK, "회원가입 성공", loginResponse);
    }

    @PostMapping("/emailCode")
    public ResTemplate<?> sendEmailCode(
            @AuthenticationPrincipal Users user,
            @RequestBody IssueEmailCodeRequest request
    ) {
        // VerificationFilter에서 Users의 VerificationStatus가 UNVERIFIED인 요청만 허용하도록 필터링됨
        // -> 여기서는 VerificationStatus를 확인할 필요 없음
        // (기획 요구사항에 따라 학교 이메일 인증이 완료된 후, 다른 이메일로 변경할 수 있어야 한다면 추가 로직 필요함)
        String email = request.emailAddress();
        mailAuthService.sendAuthCode(user.getUserId(), email);
        return new ResTemplate<>(HttpStatus.OK, "인증코드 전송 완료", null);
    }

    @PostMapping("/emailCode/verify")
    public ResTemplate<?> verifyEmailCode(
            @AuthenticationPrincipal Users user,
            @RequestBody VerifyEmailCodeRequest request
    ) {
        // 여기서도 VerificationFilter에서 Users의 VerificationStatus가 UNVERIFIED인 요청만 허용하도록 필터링됨
        String email = request.emailAddress();
        String code = request.authCode();

        // 인증코드 검증
        // 인증코드 만료 또는 미발급은 예외 반환
        boolean verifyResult = mailAuthService.verifyAuthCode(user.getUserId(), email, code);
        if(verifyResult) {
            usersService.verifyUniversityEmail(user, email);
            return new ResTemplate<>(HttpStatus.OK, "인증이 완료되었습니다.", null);
        } else {
            return new ResTemplate<>(HttpStatus.I_AM_A_TEAPOT, "인증코드 검증에 실패하였습니다.", null);
        }
    }
}
