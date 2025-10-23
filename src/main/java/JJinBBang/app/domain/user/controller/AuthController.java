package JJinBBang.app.domain.user.controller;

import static JJinBBang.app.global.cookie.CookieType.*;


import JJinBBang.app.domain.user.dto.request.IssueEmailCodeRequest;
import JJinBBang.app.domain.user.dto.request.VerifyEmailCodeRequest;
import JJinBBang.app.domain.user.dto.response.TokenResponse;
import JJinBBang.app.domain.user.entity.PendingUser;
import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.domain.user.exception.UserAuthException;
import JJinBBang.app.domain.user.exception.UserNotFoundException;
import JJinBBang.app.domain.user.repository.PendingUserRepository;
import JJinBBang.app.domain.user.service.UsersService;
import JJinBBang.app.global.cookie.CookieUtils;
import JJinBBang.app.global.jwt.dto.TokenPair;
import JJinBBang.app.global.jwt.service.JwtService;
import JJinBBang.app.global.mail.service.MailAuthService;
import JJinBBang.app.global.template.ResTemplate;
import jakarta.servlet.http.HttpServletResponse;
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

    private final UsersService usersService;
    private final MailAuthService mailAuthService;
    private final PendingUserRepository pendingUserRepository;
    private final JwtService jwtService;
    private final CookieUtils cookieUtils;

    @PostMapping("/signup")
    public ResTemplate<TokenResponse> signUp(
        @CookieValue(value = PENDING_TOKEN_COOKIE, required = false) String pendingToken,
        HttpServletResponse res
    ) {
        if(pendingToken == null || pendingToken.isBlank()) {
            throw UserAuthException.loginSessionExpired();
        }

        // 로그인 한 유저에 대해 약관 동의를 수행하는 API 입니다.
        PendingUser pendingUser = pendingUserRepository.findById(pendingToken)
            .orElseThrow(UserNotFoundException::notFound);

        Users user = Users.builder()
            .provider(pendingUser.provider())
            .providerId(pendingUser.providerId())
            .build();

        Users save = usersService.save(user);

        TokenPair tokenPair = jwtService.generateTokenPair(save);
        cookieUtils.addCookie(res, REFRESH_TOKEN_COOKIE, tokenPair.refreshToken(), null);

        pendingUserRepository.delete(pendingToken);
        cookieUtils.deleteCookie(res, PENDING_TOKEN_COOKIE);

        return new ResTemplate<>(HttpStatus.OK, "회원가입 성공", TokenResponse.of(tokenPair.accessToken()));
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
        if (verifyResult) {
            usersService.verifyUniversityEmail(user, email);
            mailAuthService.deleteAuthCode(user.getUserId());
            return new ResTemplate<>(HttpStatus.OK, "인증이 완료되었습니다.", null);
        } else {
            return new ResTemplate<>(HttpStatus.I_AM_A_TEAPOT, "인증코드 검증에 실패하였습니다.", null);
        }
    }

    @PutMapping("/tokenRefresh")
    public ResTemplate<TokenResponse> reissueAccessToken(
        @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
        HttpServletResponse res
    ) {
        if(refreshToken == null || refreshToken.isBlank()) {
            throw UserAuthException.loginSessionExpired();
        }
        var claims = jwtService.parseClaims(refreshToken);
        Long userId = Long.valueOf(claims.getSubject());
        Users user = usersService.findById(userId);
        TokenPair rotate;
        try {
            rotate = jwtService.rotate(user, refreshToken);
        } catch (Exception e){
            cookieUtils.deleteCookie(res, REFRESH_TOKEN_COOKIE);
            throw e;
        }
        cookieUtils.addCookie(res, REFRESH_TOKEN_COOKIE, rotate.refreshToken(), null);

        return new ResTemplate<>(HttpStatus.OK, "엑세스 토큰, 리프레시 토큰 재발급 성공", TokenResponse.of(rotate.accessToken()));
    }

    @DeleteMapping("/logout")
    public ResTemplate<?> logout(
        @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
        HttpServletResponse res
    ) {
        if(refreshToken == null || refreshToken.isBlank()) {
            throw UserAuthException.loginSessionExpired();
        }
        var claims = jwtService.parseClaims(refreshToken);
        Long userId = Long.valueOf(claims.getSubject());
        jwtService.logout(userId, refreshToken);
        cookieUtils.deleteCookie(res, REFRESH_TOKEN_COOKIE);
        return new ResTemplate<>(HttpStatus.OK, "로그아웃 성공", null);
    }

    @DeleteMapping("/logout-all")
    public ResTemplate<?> logoutAll(
        @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
        HttpServletResponse res
    ) {
        if(refreshToken == null || refreshToken.isBlank()) {
            throw UserAuthException.loginSessionExpired();
        }
        var claims = jwtService.parseClaims(refreshToken);
        Long userId = Long.valueOf(claims.getSubject());
        jwtService.logoutAll(userId);
        cookieUtils.deleteCookie(res, REFRESH_TOKEN_COOKIE);
        return new ResTemplate<>(HttpStatus.OK, "로그아웃 성공", null);
    }

    @DeleteMapping("/user")
    public ResTemplate<?> deleteUser(
        @AuthenticationPrincipal Users user,
        @CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
        HttpServletResponse res
    ) {
        if(refreshToken == null || refreshToken.isBlank()) {
            throw UserAuthException.loginSessionExpired();
        }
        jwtService.logoutAll(user.getUserId());
        cookieUtils.deleteCookie(res, REFRESH_TOKEN_COOKIE);
        usersService.deleteUser(user);
        return new ResTemplate<>(HttpStatus.OK, "회원 탈퇴 성공", null);
    }
}
