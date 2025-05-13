package JJinBBang.app.domain.building.repository;

import JJinBBang.app.domain.building.dto.GetUserBookmarkRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface BookmarkRepository {

    Page<Object[]> findLikedItemsByUserIdNative(@Param("userId") Long userId, Pageable pageable, GetUserBookmarkRequest request);

}
