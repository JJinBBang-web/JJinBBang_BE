package JJinBBang.app.global.security;

import static JJinBBang.app.global.security.properties.SecurityPathProperties.*;

import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import JJinBBang.app.global.jwt.JwtAuthenticationFilter;
import JJinBBang.app.global.security.filter.VerificationStatusFilter;
import JJinBBang.app.global.security.handler.OAuth2AuthenticationFailureHandler;
import JJinBBang.app.global.security.handler.OAuth2AuthenticationSuccessHandler;
import JJinBBang.app.global.security.properties.SecurityPathProperties;
import JJinBBang.app.global.security.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import JJinBBang.app.global.security.resolver.CustomOAuth2AuthorizationRequestResolver;
import JJinBBang.app.global.security.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final SecurityPathProperties securityPathProperties;
	private final AuthenticationEntryPoint authenticationEntryPoint;
	private final AccessDeniedHandler accessDeniedHandler;
	private final VerificationStatusFilter verificationStatusFilter;

	private final HttpCookieOAuth2AuthorizationRequestRepository authRequestRepository;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2AuthenticationSuccessHandler authenticationSuccessHandler;
	private final OAuth2AuthenticationFailureHandler authenticationFailureHandler;

	private final ClientRegistrationRepository clientRegistrationRepository;

	private static final String OAUTH2_AUTHORIZATION_BASE_URI = "/api/v1/auth/signIn";

	// CORS 설정
	CorsConfigurationSource corsConfigurationSource() {

		CorsConfiguration globalConfig = new CorsConfiguration();
		// setAllowCredentials(true)와 함께 사용할 때는 setAllowedOriginPatterns 사용
		globalConfig.setAllowedOriginPatterns(List.of(
				"https://localhost:3000",
				"http://localhost:3000",
				"http://localhost:5173",
				"https://www.jjinbbang.kr",
				"https://*.jjinbbang.kr",  // 서브도메인 포함 (test.jjinbbang.kr 등)
				"https://www.test.jjinbbang.kr"));
		globalConfig.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS", "HEAD"));
		globalConfig.setAllowedHeaders(List.of("*"));
		globalConfig.setAllowCredentials(true);
		globalConfig.setExposedHeaders(List.of("*"));  // 클라이언트에서 접근 가능한 헤더

		CorsConfiguration swaggerConfig = new CorsConfiguration();
		swaggerConfig.addAllowedOriginPattern("http://localhost:*");
		swaggerConfig.setAllowedMethods(List.of("GET", "OPTIONS"));
		swaggerConfig.setAllowedHeaders(List.of("*"));
		swaggerConfig.setAllowCredentials(false);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", globalConfig); // 기본 API 설정
		source.registerCorsConfiguration("/swagger-ui/**", swaggerConfig); // Swagger UI 전용 설정
		source.registerCorsConfiguration("/v3/api-docs/**", swaggerConfig); // OpenAPI docs 설정
		return source;
	}

	@Bean
	public OAuth2AuthorizationRequestResolver customOAuth2AuthorizationRequestResolver() {
		return new CustomOAuth2AuthorizationRequestResolver(
				clientRegistrationRepository,
				OAUTH2_AUTHORIZATION_BASE_URI);
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.httpBasic(HttpBasicConfigurer::disable)
				.cors(c -> c.configurationSource(corsConfigurationSource()))
				.csrf(AbstractHttpConfigurer::disable)
				.exceptionHandling(e -> e
						.authenticationEntryPoint(authenticationEntryPoint)
						.accessDeniedHandler(accessDeniedHandler))
				.authorizeHttpRequests(this::authorizeSetting)

				.oauth2Login(o -> o
						.authorizationEndpoint(au -> au
								.baseUri(OAUTH2_AUTHORIZATION_BASE_URI)
								.authorizationRequestRepository(authRequestRepository)
								.authorizationRequestResolver(customOAuth2AuthorizationRequestResolver()))
						.redirectionEndpoint(re -> re
								.baseUri("/login/oauth2/code/*"))
						.userInfoEndpoint(ui -> ui
								.userService(customOAuth2UserService))
						.successHandler(authenticationSuccessHandler)
						.failureHandler(authenticationFailureHandler))
				.addFilterBefore(verificationStatusFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(jwtAuthenticationFilter, VerificationStatusFilter.class);

		return http.build();
	}

	private void authorizeSetting(
			AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorize) {
		// 특정 메서드 우선 → ALL
		// 1) permitAll 등록된 경로 권한 설정
		Map<String, List<String>> permit = securityPathProperties.getPermitAll();
		permit.entrySet().stream()
				.filter(e -> !METHOD_ALL.equalsIgnoreCase(e.getKey()))
				.forEach(e -> {
					HttpMethod m = HttpMethod.valueOf(e.getKey());
					e.getValue().forEach(p -> authorize.requestMatchers(m, p).permitAll());
				});
		if (permit.containsKey(METHOD_ALL)) {
			permit.get(METHOD_ALL).forEach(p -> authorize.requestMatchers(p).permitAll());
		}

		// 2) authenticated 등록된 경로 권한 설정
		Map<String, List<String>> auth = securityPathProperties.getAuthenticated();
		auth.entrySet().stream()
				.filter(e -> !METHOD_ALL.equalsIgnoreCase(e.getKey()))
				.forEach(e -> {
					HttpMethod m = HttpMethod.valueOf(e.getKey());
					e.getValue().forEach(p -> authorize.requestMatchers(m, p).authenticated());
				});
		if (auth.containsKey(METHOD_ALL)) {
			auth.get(METHOD_ALL).forEach(p -> authorize.requestMatchers(p).authenticated());
		}

		// 3) anonymous 등록된 경로 권한 설정
		Map<String, List<String>> anon = securityPathProperties.getAnonymous();
		anon.entrySet().stream()
				.filter(e -> !METHOD_ALL.equalsIgnoreCase(e.getKey()))
				.forEach(e -> {
					HttpMethod m = HttpMethod.valueOf(e.getKey());
					e.getValue().forEach(p -> authorize.requestMatchers(m, p).anonymous());
				});
		if (anon.containsKey(METHOD_ALL)) {
			anon.get(METHOD_ALL).forEach(p -> authorize.requestMatchers(p).anonymous());
		}

		// anyRequest
		switch (securityPathProperties.getAnyRequest()) {
			case "permit-all" -> authorize.anyRequest().permitAll();
			case "anonymous" -> authorize.anyRequest().anonymous();
			default -> authorize.anyRequest().authenticated();
		}
	}
}
