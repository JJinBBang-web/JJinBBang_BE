package JJinBBang.app.domain.building.enums;

import lombok.Getter;

@Getter
public enum Floor {
	BASEMENT("반지하"), // 반지하
	LOW("저층"),      // 저층
	MID("중층"),      // 중층
	HIGH("고층"),     // 고층
	ATTIC("옥탑");    // 옥탑

	private final String description;

	Floor(String description) {this.description = description;}
}
