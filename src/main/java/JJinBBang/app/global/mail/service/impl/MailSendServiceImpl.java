package JJinBBang.app.global.mail.service.impl;

import JJinBBang.app.global.mail.exception.MailInternalException;
import JJinBBang.app.global.mail.service.MailSendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailSendServiceImpl implements MailSendService {

    private final JavaMailSender mailSender;

    @Override
    public void sendMail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw MailInternalException.sendFail();
        }
    }
}
