package JJinBBang.app.global.security.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import JJinBBang.app.global.jwt.exception.InvalidTokenException;
import JJinBBang.app.global.security.exception.SecurityAccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import JJinBBang.app.global.security.exception.SecurityAuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
		throws IOException {

		if(authException instanceof SecurityAuthException || authException instanceof InvalidTokenException) {
			makeErrorResponse(response, authException);
		} else {
			makeErrorResponse(response, SecurityAuthException.noAuthentication());
		}
	}

	private void makeErrorResponse(HttpServletResponse response, Exception exception) throws IOException {
		response.setContentType("application/json;charset=UTF-8");
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		Map<String, Object> errorDetails = new HashMap<>();
		errorDetails.put("code", HttpServletResponse.SC_UNAUTHORIZED);
		errorDetails.put("message", exception.getMessage());
		ObjectMapper mapper = new ObjectMapper();
		response.getWriter().write(mapper.writeValueAsString(errorDetails));
	}
}
