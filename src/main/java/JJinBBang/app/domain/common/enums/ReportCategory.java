package JJinBBang.app.domain.common.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ReportCategory {
    REAL_ESTATE("부동산"), // 부동산
    TIPS("자취 꿀팁"), // 자취 꿀팁
    CAMPUS_LIFE("대학 생활"), // 대학 생활
    MOVING("이사 관련"), // 이사 관련
    INTERVIEW("인터뷰"); // 인터뷰

    private final String label;

    ReportCategory(String label) {
        this.label = label;
    }

    public static ReportCategory from(String value) {
        String v = value.trim();
        return Arrays.stream(values())
                .filter(category -> category.name().equalsIgnoreCase(v) || category.label.equals(v))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Category: " + v));
    }
}
