package JJinBBang.app.domain.building.dto;

import java.math.BigDecimal;

import JJinBBang.app.domain.building.enums.ContractType;
import JJinBBang.app.domain.building.enums.Floor;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

}
