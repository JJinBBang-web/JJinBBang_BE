package JJinBBang.app.domain.user.exception;

import JJinBBang.app.global.error.exception.AuthGroupException;

public class KakaoAuthException extends AuthGroupException {
    public KakaoAuthException(String message) {
        super(message);
    }

    public static KakaoAuthException existAccount(){
        return new KakaoAuthException("이미 회원가입된 계정입니다.");
    }

    public static KakaoAuthException accessTokenGenerateError(){
        return new KakaoAuthException("엑세스 토큰 발급에 실패하였습니다.");
    }

    public static KakaoAuthException userInfoFetchFailed(){
        return new KakaoAuthException("엑세스 토큰으로 유저 정보 조회에 실패하였습니다.");
    }
}
