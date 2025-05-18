package JJinBBang.app.global.jwt.service.impl;

import JJinBBang.app.domain.user.entity.Users;
import JJinBBang.app.global.jwt.repository.RefreshTokenRepository;
import JJinBBang.app.global.jwt.service.AbstractTokenGenerateService;
import JJinBBang.app.global.jwt.service.RefreshTokenService;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service("refreshTokenService")
public class RefreshTokenGenerateServiceImpl extends AbstractTokenGenerateService implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenGenerateServiceImpl(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration-time.refresh-token}") long refreshTokenExpiration,
            RefreshTokenRepository refreshTokenRepository
    ) {
        super(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)), refreshTokenExpiration, "auth");
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public String generateToken(Users user) {
        String refreshToken = super.generateToken(user);

        if(refreshTokenRepository.findByUserId(user.getUserId()).isEmpty()){
            return refreshTokenRepository.save(user.getUserId(), refreshToken, super.expirationTime);
        }
        // 기존 리프레시 토큰이 존재하는 경우, 새로운 리프레시 토큰을 저장하고 반환
        // TODO : 블랙리스트 추가 로직 구현
        refreshTokenRepository.deleteByUserId(user.getUserId());
        return refreshTokenRepository.save(user.getUserId(), refreshToken, super.expirationTime);
    }

    @Override
    public boolean validateRefreshToken(Long userId, String refreshToken) {
        Optional<String> opt = refreshTokenRepository.findByUserId(userId);
        if(opt.isPresent()){
            String token = opt.get();
            return token.equals(refreshToken);
        }
        return false;
    }
}
