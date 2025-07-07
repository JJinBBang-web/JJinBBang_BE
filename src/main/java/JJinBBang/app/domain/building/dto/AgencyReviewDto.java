package JJinBBang.app.domain.building.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class AgencyReviewDto {
	@NotNull
	@Min(1) @Max(5)
	private BigDecimal rating;
	@NotBlank
	private String content;
}
