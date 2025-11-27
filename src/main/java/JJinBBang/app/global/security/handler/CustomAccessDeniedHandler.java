package JJinBBang.app.global.security.handler;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import JJinBBang.app.global.security.exception.SecurityAccessDeniedException;
import JJinBBang.app.global.security.exception.SecurityErrorResponder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
	private final SecurityErrorResponder responder;

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
		AccessDeniedException accessDeniedException) throws IOException, ServletException {

		if(accessDeniedException instanceof SecurityAccessDeniedException) {
			responder.write(response, accessDeniedException, HttpStatus.FORBIDDEN);
		} else {
			responder.write(response, SecurityAccessDeniedException.noAuthority(), HttpStatus.FORBIDDEN);
		}
	}
}
