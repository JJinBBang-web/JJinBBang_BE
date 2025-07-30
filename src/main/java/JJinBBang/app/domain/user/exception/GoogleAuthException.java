package JJinBBang.app.domain.user.exception;

import JJinBBang.app.global.error.exception.AuthGroupException;

public class GoogleAuthException extends AuthGroupException {
    public GoogleAuthException(String message) {
        super(message);
    }

    public static GoogleAuthException existAccount(){
        return new GoogleAuthException("이미 회원가입된 계정입니다.");
    }

    public static GoogleAuthException accessTokenGenerateError(){
        return new GoogleAuthException("구글 API 엑세스 토큰 발급에 실패하였습니다.");
    }

    public static GoogleAuthException userInfoFetchFailed(){
        return new GoogleAuthException("구글 API 엑세스 토큰으로 유저 정보 조회에 실패하였습니다.");
    }
}