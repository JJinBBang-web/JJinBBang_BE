package JJinBBang.app.global.mail.service;

public interface MailSendService {
    void sendMail(String toEmail, String subject, String body);
}
