package JJinBBang.app.domain.building.enums;

import lombok.Getter;

@Getter
public enum ContractType {
    MONTHLY_RENT("월세"), // 월세
    DEPOSIT_RENT("전세"); // 전세

    private final String description;

    ContractType(String description) {
        this.description = description;
    }
}
