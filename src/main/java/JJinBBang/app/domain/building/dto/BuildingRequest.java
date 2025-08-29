package JJinBBang.app.domain.building.dto;


import JJinBBang.app.domain.building.entity.Agencies;
import JJinBBang.app.domain.building.entity.Buildings;
import JJinBBang.app.domain.building.enums.BuildingType;
import JJinBBang.app.domain.common.entity.Campuses;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record BuildingRequest (
    @NotBlank
    String buildingCode,
    @NotBlank
    String name,
    @NotNull
    BuildingType type,
    @NotBlank
    String address,
    @NotNull
    Double latitude,
    @NotNull
    Double longitude
){
    public Buildings toBuildingEntity(Campuses campus) {
        return Buildings.builder()
            .buildingCode(buildingCode)
            .buildingName(name)
            .buildingType(type.toString())
            .buildingAddress(address)
            .buildingLat(latitude)
            .buildingLot(longitude)
            .buildingRating(null)
            .avgRentDeposit(null)
            .avgMonthlyRent(null)
            .avgMaintenanceCost(null)
            .avgDeposit(null)
            .reviewCount(0)
            .likeCount(0)
            .imagesCount(0)
            .campus(campus)
            .build();
    }

    public Agencies toAgencyEntity() {
        return Agencies.builder()
            .agencySerial(buildingCode)
            .name(name)
            .address(address)
            .agencyLat(latitude)
            .agencyLot(longitude)
            .rating(null)
            .reviewCount(0)
            .likeCount(0)
            .imagesCount(0)
            .build();
    }
}

