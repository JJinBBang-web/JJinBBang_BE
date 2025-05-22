package JJinBBang.app.global.mail.dto;

public record EmailAuthInfo(
        String email,
        String code,
        long timestamp
) { }