package JJinBBang.app.domain.building.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SetUserBookmarkRequest {

    String type;

    Long typeId;

    boolean liked;
}
