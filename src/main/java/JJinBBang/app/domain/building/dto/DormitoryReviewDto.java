package JJinBBang.app.domain.building.dto;

import java.math.BigDecimal;

import JJinBBang.app.domain.building.enums.Floor;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class DormitoryReviewDto {
	@NotNull
	private Long dormitoryId;
	@NotNull
	@Min(1)
	private Integer capacity;
	@NotNull @Min(0)
	private Integer dormFee;
	@NotNull
	private Floor floor;
	@NotNull @Min(1) @Max(5)
	private BigDecimal rating;
	@NotBlank
	private String content;
}
