package JJinBBang.app.global.mail;

import JJinBBang.app.global.mail.exception.MailInvalidException;
import JJinBBang.app.global.mail.properties.MailAuthProperties;
import JJinBBang.app.global.mail.repository.EmailAuthCodeRepository;
import JJinBBang.app.global.mail.service.MailSendService;
import JJinBBang.app.global.mail.service.impl.MailAuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MailAuthServiceImplTest {

    @Mock
    MailAuthProperties props;
    @Mock
    EmailAuthCodeRepository repo;
    @Mock
    MailSendService mailSender;

    @InjectMocks
    MailAuthServiceImpl service;

    @BeforeEach
    void setUp() {
        // 공통 스텁: 도메인 검사, 코드 길이, 제목·본문 포맷
        when(props.getAllowedDomain()).thenReturn(List.of("gnu.ac.kr"));
        when(props.getAuthCodeLength()).thenReturn(4);
        when(props.getSubjectText()).thenReturn("[찐빵] 인증코드");
        when(props.getBodyText()).thenReturn("코드: %s (유효:%s분)");
        when(props.getExpirationTime()).thenReturn(5L);
    }

    @Test
    void sendAuthCode_성공() {
        String email = "user@gnu.ac.kr";

        // isExistByEmail 기본 false → save, sendMail 호출 검증
        service.sendAuthCode(email);

        verify(repo).save(eq(email), anyString());
        verify(mailSender).sendMail(
                eq(email),
                eq("[찐빵] 인증코드"),
                contains("코드:")  // 본문에 코드가 포함되어야 함
        );
    }

    @Test
    void sendAuthCode_도메인불일치_예외() {
        // 허용된 도메인이 아니면
        String badEmail = "user@naver.com";

        // MailInvalidException 발생해야 함
        assertThrows(MailInvalidException.class,
                () -> service.sendAuthCode(badEmail));
    }

    @Test
    void verifyAuthCode_일치() {
        // u@gnu.ac.kr 로 발급된 인증코드가 1234일 때
        when(repo.findAuthCodeByEmail("u@gnu.ac.kr"))
                .thenReturn(Optional.of("1234"));

        // 검증할 인증코드가 1234이면 true 리턴
        assertTrue(service.verifyAuthCode("u@gnu.ac.kr", "1234"));
    }

    @Test
    void verifyAuthCode_불일치() {
        // u@gnu.ac.kr 로 발급된 인증코드가 9999일 때
        when(repo.findAuthCodeByEmail("u@gnu.ac.kr"))
                .thenReturn(Optional.of("9999"));

        // 검증할 인증코드가 1234이면 false 리턴
        assertFalse(service.verifyAuthCode("u@gnu.ac.kr", "1234"));
    }

    @Test
    void verifyAuthCode_미발급_예외() {
        // x@gnu.ac.kr 으로 조회 시 Optional.empty 가 리턴되면
        when(repo.findAuthCodeByEmail("x@gnu.ac.kr"))
                .thenReturn(Optional.empty());

        // x@gnu.ac.kr 으로 인증코드 검사 수행 시 MailInvalidException 발생해야 함
        assertThrows(MailInvalidException.class,
                () -> service.verifyAuthCode("x@gnu.ac.kr", "0000"));
    }
}