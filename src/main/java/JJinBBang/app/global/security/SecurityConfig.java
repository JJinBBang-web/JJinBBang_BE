package JJinBBang.app.global.security;

import java.util.List;
import java.util.Map;

import JJinBBang.app.domain.user.enums.UserRole;
import JJinBBang.app.global.security.filter.PendingUserFilter;
import JJinBBang.app.global.security.filter.VerificationStatusFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.
				httpBasic(HttpBasicConfigurer::disable)
				.cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource())) // CORS 설정 추가
				.csrf(AbstractHttpConfigurer::disable)
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(authenticationEntryPoint) // 401 (인증이 없는거)
						.accessDeniedHandler(accessDeniedHandler) // 403 (권한이 잘못된거)
				)
				.authorizeHttpRequests(authorize -> {
					authorize
							.requestMatchers(securityPathProperties.getPermitAll().toArray(new String[0])).permitAll()
							.requestMatchers(securityPathProperties.getAuthenticated().toArray(new String[0])).authenticated()
							.requestMatchers(securityPathProperties.getRefresh().toArray(new String[0])).authenticated()
							.requestMatchers(securityPathProperties.getAnonymous().toArray(new String[0])).anonymous()
							.requestMatchers(securityPathProperties.getAdmin().toArray(new String[0])).hasRole(UserRole.ADMIN.name())
					;


					switch (securityPathProperties.getAnyRequest()) {
						case "permit-all":
							authorize.anyRequest().permitAll();
							break;
						case "anonymous":
							authorize.anyRequest().anonymous();
							break;
						case "authenticated":
						default:
							authorize.anyRequest().authenticated();
							break;
					}
				})
				.addFilterBefore(verificationStatusFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(pendingUserFilter, VerificationStatusFilter.class)
				.addFilterBefore(jwtAuthenticationFilter, PendingUserFilter.class)

				;
		return httpSecurity.build();
	}

	/*
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.httpBasic(HttpBasicConfigurer::disable)
			.cors(c -> c.configurationSource(corsConfigurationSource()))
			.csrf(AbstractHttpConfigurer::disable)
			.exceptionHandling(e -> e.authenticationEntryPoint(customAuthenticationEntryPoint))
			.authorizeHttpRequests(authorize -> {
				// 1) permitAll: 특정 메서드 우선 → ALL
				Map<String, List<String>> permit = securityPathProperties.getPermitAll();
				permit.entrySet().stream()
					.filter(e -> !"ALL".equalsIgnoreCase(e.getKey()))
					.forEach(e -> {
						HttpMethod m = HttpMethod.valueOf(e.getKey());
						e.getValue().forEach(p -> authorize.requestMatchers(m, p).permitAll());
					});
				if (permit.containsKey("ALL")) {
					permit.get("ALL").forEach(p -> authorize.requestMatchers(p).permitAll());
				}

				// 2) authenticated
				Map<String, List<String>> auth = securityPathProperties.getAuthenticated();
				auth.entrySet().stream()
					.filter(e -> !"ALL".equalsIgnoreCase(e.getKey()))
					.forEach(e -> {
						HttpMethod m = HttpMethod.valueOf(e.getKey());
						e.getValue().forEach(p -> authorize.requestMatchers(m, p).authenticated());
					});
				if (auth.containsKey("ALL")) {
					auth.get("ALL").forEach(p -> authorize.requestMatchers(p).authenticated());
				}

				// 3) anonymous
				Map<String, List<String>> anon = securityPathProperties.getAnonymous();
				anon.entrySet().stream()
					.filter(e -> !"ALL".equalsIgnoreCase(e.getKey()))
					.forEach(e -> {
						HttpMethod m = HttpMethod.valueOf(e.getKey());
						e.getValue().forEach(p -> authorize.requestMatchers(m, p).anonymous());
					});
				if (anon.containsKey("ALL")) {
					anon.get("ALL").forEach(p -> authorize.requestMatchers(p).anonymous());
				}

				// 4) refresh (인증 처리)
				Map<String, List<String>> ref = securityPathProperties.getRefresh();
				ref.entrySet().stream()
					.filter(e -> !"ALL".equalsIgnoreCase(e.getKey()))
					.forEach(e -> {
						HttpMethod m = HttpMethod.valueOf(e.getKey());
						e.getValue().forEach(p -> authorize.requestMatchers(m, p).authenticated());
					});
				if (ref.containsKey("ALL")) {
					ref.get("ALL").forEach(p -> authorize.requestMatchers(p).authenticated());
				}

				// anyRequest
				switch (securityPathProperties.getAnyRequest()) {
					case "permit-all"  -> authorize.anyRequest().permitAll();
					case "anonymous"   -> authorize.anyRequest().anonymous();
					default            -> authorize.anyRequest().authenticated();
				}
			})
			.addFilterBefore(verificationStatusFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(pendingUserFilter, VerificationStatusFilter.class)
			.addFilterBefore(jwtAuthenticationFilter, PendingUserFilter.class);

		return http.build();
	}

	 */
}
