package JJinBBang.app.domain.building.exception;

import JJinBBang.app.global.error.exception.NotFoundGroupException;

public class RecentlyViewedNotFoundException extends NotFoundGroupException {
    public RecentlyViewedNotFoundException(String message) {
        super(message);
    }

    public static RecentlyViewedNotFoundException notreviewId() {
        return new RecentlyViewedNotFoundException("reviewIds은 필수 입력값이며 비어 있을 수 없습니다.");
    };

    public static RecentlyViewedNotFoundException longreviewId() {
        return new RecentlyViewedNotFoundException("reviewIds은 최대 5개입니다.");
    };
};
