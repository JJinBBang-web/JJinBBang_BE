package JJinBBang.app.global.security.exception;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import JJinBBang.app.global.template.ResTemplate;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SecurityErrorResponder {

	private final ObjectMapper om;

	public void write(
		HttpServletResponse res,
		Exception exception,
		HttpStatus httpStatus
	) throws IOException {
		res.setStatus(httpStatus.value());
		res.setCharacterEncoding(StandardCharsets.UTF_8.name());
		res.setContentType("application/json");

		ResTemplate<Object> errorResponse = new ResTemplate<>(
			httpStatus,
			exception.getMessage()
		);

		res.getWriter().write(om.writeValueAsString(errorResponse));
	}

}
