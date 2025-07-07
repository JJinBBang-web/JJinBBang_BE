package JJinBBang.app.domain.building.dto;

import java.math.BigDecimal;

import JJinBBang.app.domain.building.enums.ContractType;
import JJinBBang.app.domain.building.enums.Floor;
import jakarta.validation.constraints.*;
import lombok.Getter;

@Getter
public class GeneralReviewDto {
	@NotNull
	private ContractType contractType;
	@NotNull
	@Min(0)
	private Integer deposit;
	@Min(0)
	private Integer monthlyRent;     // 전세면 null 허용
	@NotNull @Min(0)
	private Integer maintenanceCost;
	@NotNull
	private Floor floor;
	@NotNull @DecimalMin("0.0")
	private Double space;
	@NotNull @Min(1) @Max(5)
	private BigDecimal rating;
	@NotBlank
	private String content;

	@AssertTrue(message = "계약 타입에 따라 deposit, monthlyRent, maintenanceCost 필드를 올바르게 설정해주세요.")
	private boolean isContractTypeValid() {
		if (contractType == null) {
			return true;  // @NotNull 에서 잡아줌
		}
        return switch (contractType) {
            case MONTHLY_RENT -> deposit != null
                    && monthlyRent != null
                    && maintenanceCost != null;
            case DEPOSIT_RENT -> deposit != null
                    && maintenanceCost != null
                    && monthlyRent == null;
        };
	}
}
