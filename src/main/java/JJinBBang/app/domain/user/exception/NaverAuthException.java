package JJinBBang.app.domain.user.exception;

public class NaverAuthException extends RuntimeException {

    public NaverAuthException(String message) {
        super(message);
    }

    public static NaverAuthException existAccount(){
        return new NaverAuthException("이미 회원가입된 계정입니다.");
    }

    public static NaverAuthException accessTokenGenerateError(){
        return new NaverAuthException("네이버 API 엑세스 토큰 발급에 실패하였습니다.");
    }

    public static NaverAuthException userInfoFetchFailed(){
        return new NaverAuthException("네이버 API 엑세스 토큰으로 유저 정보 조회에 실패하였습니다.");
    }

    public static NaverAuthException notAllowedRedirectUri() {
        return new NaverAuthException("허용되지 않은 리다이렉트 URI 입니다.");
    }
}
