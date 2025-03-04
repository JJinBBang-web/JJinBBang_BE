package JJinBBang.app.domain.building.service;

import org.springframework.stereotype.Service;

@Service
public interface BookmarkService {
    void BuildingBookmark(Long buildingId, Long userId, boolean liked);

    void ReviewBookmark(Long reviewId, Long userId, boolean liked);
}
