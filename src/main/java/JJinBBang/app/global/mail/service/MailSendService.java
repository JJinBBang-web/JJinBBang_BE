package JJinBBang.app.global.mail.service;

public interface MailSendService {
    /**
     * 이메일을 전송합니다.
     *
     * @param toEmail 수신자 이메일 주소
     * @param subject 이메일 제목
     * @param body 이메일 본문
     */
    void sendMail(String toEmail, String subject, String body);
}
