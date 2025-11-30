package JJinBBang.app.global.security;

import static JJinBBang.app.global.security.SecurityPathProperties.*;

import java.util.List;
import java.util.Map;

import JJinBBang.app.global.security.filter.PendingUserFilter;
import JJinBBang.app.global.security.filter.VerificationStatusFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import JJinBBang.app.global.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final SecurityPathProperties securityPathProperties;
	private final AuthenticationEntryPoint authenticationEntryPoint;
	private final AccessDeniedHandler accessDeniedHandler;
	private final PendingUserFilter pendingUserFilter;
	private final VerificationStatusFilter verificationStatusFilter;

	// CORS 설정
	CorsConfigurationSource corsConfigurationSource() {

		CorsConfiguration globalConfig = new CorsConfiguration();
		// globalConfig.setAllowedOrigins(List.of("http://localhost:3000"));
//		globalConfig.setAllowedOrigins(List.of("*")); // 테스트용 TODO: 위에 패턴으로 변경해야 함
		globalConfig.setAllowedOriginPatterns(List.of("*")); // 모든 도메인 허용
		globalConfig.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE"));
		globalConfig.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		// globalConfig.setAllowedHeaders(List.of("*"));
		globalConfig.setAllowCredentials(true);

		CorsConfiguration swaggerConfig = new CorsConfiguration();
		swaggerConfig.addAllowedOriginPattern("*");
		swaggerConfig.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
		swaggerConfig.setAllowedHeaders(List.of("*"));
		swaggerConfig.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", globalConfig);                  // 기본 API 설정
		source.registerCorsConfiguration("/swagger-ui/**", swaggerConfig);       // Swagger UI 전용 설정
		source.registerCorsConfiguration("/v3/api-docs/**", swaggerConfig);      // OpenAPI docs 설정
		return source;
	}


	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.httpBasic(HttpBasicConfigurer::disable)
			.cors(c -> c.configurationSource(corsConfigurationSource()))
			.csrf(AbstractHttpConfigurer::disable)
			.exceptionHandling(e -> e
				.authenticationEntryPoint(authenticationEntryPoint)
				.accessDeniedHandler(accessDeniedHandler)
			)
			.authorizeHttpRequests(this::authorizeSetting)
			.addFilterBefore(verificationStatusFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(pendingUserFilter, VerificationStatusFilter.class)
			.addFilterBefore(jwtAuthenticationFilter, PendingUserFilter.class);

		return http.build();
	}

	private void authorizeSetting(
		AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorize) {
        // 밑 주석 코드는 테스트 서버에서만 활성화 할 예정
//        authorize.requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll();
//        authorize.requestMatchers("/actuator/prometheus", "/actuator/metrics/**").permitAll();

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

		// 4) refresh 등록된 경로 권한 설정 (리프레시 토큰)
		Map<String, List<String>> ref = securityPathProperties.getRefresh();
		ref.entrySet().stream()
			.filter(e -> !METHOD_ALL.equalsIgnoreCase(e.getKey()))
			.forEach(e -> {
				HttpMethod m = HttpMethod.valueOf(e.getKey());
				e.getValue().forEach(p -> authorize.requestMatchers(m, p).authenticated());
			});
		if (ref.containsKey(METHOD_ALL)) {
			ref.get(METHOD_ALL).forEach(p -> authorize.requestMatchers(p).authenticated());
		}

		// anyRequest
		switch (securityPathProperties.getAnyRequest()) {
			case "permit-all"  -> authorize.anyRequest().permitAll();
			case "anonymous"   -> authorize.anyRequest().anonymous();
			default            -> authorize.anyRequest().authenticated();
		}
	}
}
