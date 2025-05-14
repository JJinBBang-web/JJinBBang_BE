package JJinBBang.app.domain.building.service;

import JJinBBang.app.domain.building.dto.GetUserBookmarkRequest;
import JJinBBang.app.domain.building.dto.InfoDto;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public interface BookmarkService {
    void BuildingBookmark(Long buildingId, Long userId, boolean liked);

    void ReviewBookmark(Long reviewId, Long userId, boolean liked);

    void AgencyBookmark(Long agencyId, Long userId, boolean liked);

    List<InfoDto> SearchBookmark(Long userId, Pageable pageable, GetUserBookmarkRequest request);
}
