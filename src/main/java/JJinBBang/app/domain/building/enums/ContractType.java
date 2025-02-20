package JJinBBang.app.domain.building.enums;

public enum ContractType {
    MONTHLY_RENT("월세"),
    LEASE("전세"),
    SALE("매매"),
    DORMITORY("기숙사");

    private final String description;

    ContractType(String description) {
        this.description = description;
    }
}
