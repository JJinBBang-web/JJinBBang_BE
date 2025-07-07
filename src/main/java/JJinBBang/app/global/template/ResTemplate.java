package JJinBBang.app.global.template;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@JsonPropertyOrder({"code","message","data"})
public class ResTemplate<T> {
	int code;
	String message;
	T data;

	// 성공하였을 경우
	public ResTemplate(HttpStatus httpStatus, String message, T data) {
		this.code = httpStatus.value();
		this.message = message;
		this.data = data;
		log.info("Response generated: code={}, message={}", code, message);
	}

	// 실패하였을 경우
	public ResTemplate(HttpStatus httpStatus, String message) {
		this.code = httpStatus.value();
		this.message = message;
		log.info("Response generated: code={}, message={}", code, message);
	}
}