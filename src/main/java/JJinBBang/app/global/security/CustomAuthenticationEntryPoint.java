package JJinBBang.app.global.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
		System.out.println("CustomAuthenticationEntryPoint.commence");

		if(authException instanceof SecurityAccessDeniedException) {
			makeAccessDeniedResponse(authException, response);
		} else if (authException instanceof SecurityAuthException) {
			makeUnauthorizedResponse(authException, response);
		}
		else {
			makeUnauthorizedResponse(SecurityAuthException.noAuthentication(), response);
		}
	}

	private void makeAccessDeniedResponse(Exception e, HttpServletResponse response) throws IOException {
		response.setContentType("application/json;charset=UTF-8");
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> errorDetails = new HashMap<>();

		response.setStatus(HttpServletResponse.SC_FORBIDDEN);

		errorDetails.put("code", HttpServletResponse.SC_FORBIDDEN);
		errorDetails.put("message", e.getMessage());

		response.getWriter().write(mapper.writeValueAsString(errorDetails));
	}

	private void makeUnauthorizedResponse(Exception e, HttpServletResponse response) throws IOException {
		response.setContentType("application/json;charset=UTF-8");
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> errorDetails = new HashMap<>();

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

		errorDetails.put("code", HttpServletResponse.SC_UNAUTHORIZED);
		errorDetails.put("message", e.getMessage());

		response.getWriter().write(mapper.writeValueAsString(errorDetails));
	}

}
