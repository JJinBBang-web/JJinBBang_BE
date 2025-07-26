package JJinBBang.app.domain.common.dto;

import JJinBBang.app.domain.common.entity.Campuses;
import JJinBBang.app.domain.common.entity.Universities;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public record UniversityResponseDto(
        Long id,
        String universityName,
        String universityLogo,
        List<UnivCampusListResponse.CampusDTO> campuses
) {
   public static UniversityResponseDto of(Universities university) {
       return new UniversityResponseDto(
               university.getId(),
               university.getUniversityName(),
               university.getUniversityLogo(),
               UnivCampusListResponse.CampusDTO.from(university.getCampuses()).getCampusList()
       );
   }
}
