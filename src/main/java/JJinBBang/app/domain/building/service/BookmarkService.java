package JJinBBang.app.domain.building.service;

import JJinBBang.app.domain.building.dto.GetUserBookmarkRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public interface BookmarkService {
    void BuildingBookmark(Long buildingId, Long userId, boolean liked);

    void ReviewBookmark(Long reviewId, Long userId, boolean liked);

    void AgencyBookmark(Long agencyId, Long userId, boolean liked);

    Page<Object[]> SearchBookmark(Long userId, Pageable pageable, GetUserBookmarkRequest request);
}
