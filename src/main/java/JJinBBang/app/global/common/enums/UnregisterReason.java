package JJinBBang.app.global.common.enums;

import JJinBBang.app.domain.user.exception.OpinionException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum UnregisterReason {
    REASON_1(1, "대체 서비스로 이동"),
    REASON_2(2, "개인정보/보안 우려"),
    REASON_3(3, "알림/마케팅 메시지가 많음"),
    REASON_4(4, "일시 사용 중단을 위해"),
    REASON_5(5, "사용 빈도가 낮음"),
    REASON_6(6, "이용이 불편하고 장애가 많음");


    private final int number;
    private final String description;

    public static UnregisterReason fromNumber(Integer number) {
        if (number == null) throw OpinionException.NoOptionException();

        return Arrays.stream(values())
                .filter(unregisterReason -> unregisterReason.number == number)
                .findFirst()
                .orElseThrow(OpinionException::NoOptionException);
    }
}
