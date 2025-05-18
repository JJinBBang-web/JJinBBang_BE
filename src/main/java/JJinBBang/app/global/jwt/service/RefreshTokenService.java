package JJinBBang.app.global.jwt.service;

public interface RefreshTokenService {

    boolean validateRefreshToken(Long userId, String refreshToken);

}
