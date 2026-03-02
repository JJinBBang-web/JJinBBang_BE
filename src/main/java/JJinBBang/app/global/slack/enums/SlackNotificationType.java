package JJinBBang.app.global.slack.enums;

public enum SlackNotificationType {
    OPINION("[문의 및 신고가 접수되었습니다. - ID: %s] \n%s"),
    CERTIFICATE("[재학생 인증 요청 - ID: %s] \n관리자 확인이 필요합니다.\n\n📄 <%s|합격증명서 확인하기>");

    private final String template;

    SlackNotificationType(String template) {
        this.template = template;
    }

    public String format(Object... args) {
        return String.format(template, args);
    }
}
