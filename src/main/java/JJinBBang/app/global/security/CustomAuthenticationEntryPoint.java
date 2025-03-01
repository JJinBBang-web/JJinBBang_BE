package JJinBBang.app.global.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

		SecurityAuthException exception = SecurityAuthException.noAuthentication(); // 예외 들고오기

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json;charset=UTF-8");

		Map<String, Object> errorDetails = new HashMap<>();
		errorDetails.put("code", HttpServletResponse.SC_UNAUTHORIZED);
		errorDetails.put("message", exception.getMessage());

		ObjectMapper mapper = new ObjectMapper();
		response.getWriter().write(mapper.writeValueAsString(errorDetails));
	}

}
