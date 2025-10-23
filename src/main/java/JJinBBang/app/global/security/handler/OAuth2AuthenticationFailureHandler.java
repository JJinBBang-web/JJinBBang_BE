package JJinBBang.app.global.security.handler;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import JJinBBang.app.global.security.exception.SecurityAuthException;
import JJinBBang.app.global.security.exception.SecurityErrorResponder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

	private final SecurityErrorResponder responder;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws
		IOException {
		responder.write(
			response,
			SecurityAuthException.oauthAuthenticationFailure(),
			HttpStatus.UNAUTHORIZED
		);
	}
}
