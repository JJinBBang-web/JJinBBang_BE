package JJinBBang.app.domain.common.dto;

import JJinBBang.app.domain.common.entity.Campuses;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class UnivCampusListResponse {
    private List<CampusDTO> campusList;

    @Builder
    public record CampusDTO(
            Long id,
            String campusName,
            String logoImageUrl,
            String campusAddress,
            double latitude,
            double longitude
    ){
        public static UnivCampusListResponse from(List<Campuses> campuses){
            List<CampusDTO> campusDTOs = new ArrayList<>();
            for (Campuses campus : campuses) {
                CampusDTO dto = CampusDTO.builder()
                        .id(campus.getId())
                        .campusName(campus.getCampusName())
                        .logoImageUrl(campus.getImage()) // ✅ logoImageUrl 추가
                        .campusAddress(campus.getCampusAddress())
                        .latitude(campus.getCampusLat())
                        .longitude(campus.getCampusLot())
                        .build();
                campusDTOs.add(dto);
            }
            return new UnivCampusListResponse(campusDTOs);
        }
    }
}

