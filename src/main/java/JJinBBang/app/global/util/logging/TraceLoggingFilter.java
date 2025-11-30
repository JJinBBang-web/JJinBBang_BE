package JJinBBang.app.global.util.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import JJinBBang.app.domain.user.entity.Users;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class TraceLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TraceLoggingFilter.class);
    private static final String HEADER_REQUEST_ID = "X-Request-Id";
    private static final String HEADER_TRACE_ID = "X-B3-TraceId";

    private final String serviceName;
    private final String activeProfile;

    public TraceLoggingFilter(Environment environment) {
        this.serviceName = environment.getProperty("spring.application.name", "jjinbbang-be");
        this.activeProfile = environment.getProperty("spring.profiles.active", "default");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long startTime = System.nanoTime();
        String traceId = normalizeTraceId(firstNonBlank(request.getHeader(HEADER_TRACE_ID), MDC.get("traceId")));
        String spanId = generateSpanId();
        String requestId = firstNonBlank(request.getHeader(HEADER_REQUEST_ID), UUID.randomUUID().toString());

        MDC.put("service", serviceName);
        MDC.put("environment", activeProfile);
        MDC.put("traceId", traceId);
        MDC.put("spanId", spanId);
        MDC.put("requestId", requestId);
        MDC.put("httpMethod", request.getMethod());
        MDC.put("uri", request.getRequestURI());
        MDC.put("remoteIp", resolveClientIp(request));
        Optional.ofNullable(request.getQueryString()).filter(StringUtils::hasText).ifPresent(q -> MDC.put("query", q));
        Optional.ofNullable(request.getHeader("User-Agent")).filter(StringUtils::hasText).ifPresent(ua -> MDC.put("userAgent", ua));

        response.setHeader("X-Trace-Id", traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            MDC.put("status", String.valueOf(response.getStatus()));
            MDC.put("latencyMs", String.valueOf(durationMs));

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof Users user) {
                Optional.ofNullable(user.getUserId()).ifPresent(id -> MDC.put("userId", String.valueOf(id)));
                Optional.ofNullable(user.getProvider()).ifPresent(provider -> MDC.put("oauthProvider", provider.name()));
            }

            log.info("{} {} -> {}", request.getMethod(), request.getRequestURI(), response.getStatus());

            clearMdc();
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private static String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp;
        }
        return request.getRemoteAddr();
    }

    private static String normalizeTraceId(String traceId) {
        if (!StringUtils.hasText(traceId)) {
            return generateTraceId();
        }
        String sanitized = traceId.replace("-", "");
        if (sanitized.length() > 32) {
            return sanitized.substring(sanitized.length() - 32);
        }
        if (sanitized.length() < 32) {
            return String.format("%1$32s", sanitized).replace(' ', '0');
        }
        return sanitized;
    }

    private static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private static String generateSpanId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private static void clearMdc() {
        MDC.remove("service");
        MDC.remove("environment");
        MDC.remove("traceId");
        MDC.remove("spanId");
        MDC.remove("requestId");
        MDC.remove("httpMethod");
        MDC.remove("uri");
        MDC.remove("remoteIp");
        MDC.remove("query");
        MDC.remove("userAgent");
        MDC.remove("status");
        MDC.remove("latencyMs");
        MDC.remove("userId");
        MDC.remove("oauthProvider");
    }
}