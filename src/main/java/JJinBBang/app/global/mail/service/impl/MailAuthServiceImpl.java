package JJinBBang.app.global.mail.service.impl;

import JJinBBang.app.global.mail.exception.MailInvalidException;
import JJinBBang.app.global.mail.properties.MailAuthProperties;
import JJinBBang.app.global.mail.repository.EmailAuthCodeRepository;
import JJinBBang.app.global.mail.service.MailAuthService;
import JJinBBang.app.global.mail.service.MailSendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailAuthServiceImpl implements MailAuthService {
    private final MailAuthProperties properties;
    private final Random random = new Random();
    private final MailSendService mailSendService;
    private final EmailAuthCodeRepository emailAuthCodeRepository;

    @Override
    public void sendAuthCode(String toEmail) {
        validateEmail(toEmail);

        String authCode = generateAuthCode();
        saveAuthCode(toEmail, authCode);

        mailSendService.sendMail(toEmail, properties.getSubjectText(), buildEmailBody(authCode));
    }

    @Override
    public boolean verifyAuthCode(String email, String authCode) {
        String savedAuthCode = emailAuthCodeRepository.findAuthCodeByEmail(email)
                .orElseThrow(MailInvalidException::notFoundAuthCode);
        return savedAuthCode.equals(authCode);
    }


    // utility --------------------------------------------------------------------------------------------------------
    public String generateAuthCode() {
        // emailAuthCodeLength 자리 숫자로 구성된 인증 코드 생성
        // 앞자리 0이 있어도 텍스트로 생성
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < properties.getAuthCodeLength(); i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    public void saveAuthCode(String email, String authCode) {
        if(emailAuthCodeRepository.isExistByEmail(email)) {
            // 이미 인증 코드가 발급된 이메일인 경우
            log.info("이미 인증 코드가 발급된 이메일입니다. [email: {}]. 기존 인증 코드를 삭제합니다.", email);
            emailAuthCodeRepository.deleteByEmail(email);
        }
        emailAuthCodeRepository.save(email, authCode);
    }

    private String buildEmailBody(String code) {
        return String.format(properties.getBodyText(), code, properties.getExpirationTime());
    }
    // ----------------------------------------------------------------------------------------------------------------

    // validator ------------------------------------------------------------------------------------------------------
    private void validateEmail(String email) {
        if (!isValidFormat(email)) {
            throw MailInvalidException.invalidEmailFormat();
        }
        if (!properties.getAllowedDomain().contains(extractDomain(email))) {
            throw MailInvalidException.invalidEmailDomain();
        }
    }

    private boolean isValidFormat(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private String extractDomain(String email) {
        int atIdx = email.lastIndexOf("@");
        return (atIdx != -1 && atIdx < email.length() - 1)
                ? email.substring(atIdx + 1)
                : "";
    }
    // ----------------------------------------------------------------------------------------------------------------
}
