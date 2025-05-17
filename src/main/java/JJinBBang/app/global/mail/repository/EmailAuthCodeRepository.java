package JJinBBang.app.global.mail.repository;

import JJinBBang.app.global.mail.dto.EmailAuthInfo;

import java.util.Optional;

public interface EmailAuthCodeRepository {
//     key: userId
//     value: EmailAuthInfo(email, authCode, timestamp)
//     key를 userId로 함으로써 인증코드 저장을 유저 단위로 구분함.

    /**
     * 인증코드를 저장합니다.
     * @param userId 인증코드를 발급받은 유저의 아이디
     * @param email 인증코드를 발급받은 유저의 이메일
     * @param authCode 인증코드
     */
    void save(Long userId, String email, String authCode);

    /**
     * 주어진 userId에 대한 인증코드가 존재하는지 확인합니다.
     * @param userId 인증코드를 발급받은 유저의 아이디
     * @return 존재하면 true, 만료되었거나 존재하지 않으면 false
     */
    boolean isExistByUserId(Long userId);

    /**
     * 주어진 userId에 대한 이메일과 인증코드를 조회합니다.
     * @param userId 인증코드를 발급받은 유저의 아이디
     * @return 존재하면 EmailAuthInfo, 만료되었거나 존재하지 않으면 Optional.empty()
     */

    Optional<EmailAuthInfo> findEmailAndAuthCodeByUserId(Long userId);

    /**
     * 주어진 userId에 대한 인증코드를 삭제합니다.
     * @param userId 인증코드를 발급받은 유저의 아이디
     */

    void deleteByUserId(Long userId);
}
