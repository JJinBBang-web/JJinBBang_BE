package JJinBBang.app.global.security.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import JJinBBang.app.global.security.exception.SecurityAccessDeniedException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
		AccessDeniedException accessDeniedException) throws IOException, ServletException {
		System.out.println("accessDeniedException = " + accessDeniedException.getMessage());

		if(accessDeniedException instanceof SecurityAccessDeniedException) {
			makeErrorResponse(response, accessDeniedException);
		} else {
			makeErrorResponse(response, SecurityAccessDeniedException.noAuthority());
		}
	}

	private void makeErrorResponse(HttpServletResponse response, Exception exception) throws IOException {
		response.setContentType("application/json;charset=UTF-8");
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		Map<String, Object> errorDetails = new HashMap<>();
		errorDetails.put("code", HttpServletResponse.SC_FORBIDDEN);
		errorDetails.put("message", exception.getMessage());
		ObjectMapper mapper = new ObjectMapper();
		response.getWriter().write(mapper.writeValueAsString(errorDetails));
	}
}
