package JJinBBang.app.domain.common.enums;

public enum ReportCategory {
    REAL_ESTATE, // 부동산
    TIPS, // 자취 꿀팁
    CAMPUS_LIFE, // 대학 생활
    MOVING, // 이사 관련
    INTERVIEW; // 인터뷰

    public static ReportCategory from(String value) {
        try {
            return ReportCategory.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
