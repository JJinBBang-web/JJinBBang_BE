package JJinBBang.app.domain.building.service;

import JJinBBang.app.domain.building.dto.GetUserBookmarkRequest;
import JJinBBang.app.global.common.dto.InfoDto;
import JJinBBang.app.domain.user.entity.Users;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public interface BookmarkService {
    void buildingBookmark(Long buildingId, Users user, boolean liked);

    void reviewBookmark(Long reviewId, Users user, boolean liked);

    void agencyBookmark(Long agencyId, Users user, boolean liked);

    List<InfoDto> searchBookmark(Long userId, Pageable pageable, GetUserBookmarkRequest request);
}
