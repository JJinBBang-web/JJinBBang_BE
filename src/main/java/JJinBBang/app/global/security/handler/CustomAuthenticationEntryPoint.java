package JJinBBang.app.global.security.handler;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import JJinBBang.app.global.jwt.exception.InvalidTokenException;
import JJinBBang.app.global.security.exception.SecurityAuthException;
import JJinBBang.app.global.security.exception.SecurityErrorResponder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final SecurityErrorResponder responder;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
		throws IOException {

		if(authException instanceof SecurityAuthException || authException instanceof InvalidTokenException) {
			responder.write(response, authException, HttpStatus.UNAUTHORIZED);
		} else {
			responder.write(response, SecurityAuthException.noAuthentication(), HttpStatus.UNAUTHORIZED);
		}
	}
}
