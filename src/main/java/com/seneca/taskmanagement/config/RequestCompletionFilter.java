package com.seneca.taskmanagement.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.seneca.taskmanagement.util.LoggingUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class RequestCompletionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        String requestId = MDC.get("requestId");
        String clientIp = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("method", request.getMethod());
            metadata.put("path", request.getRequestURI());
            metadata.put("status", status);
            metadata.put("duration_ms", duration);
            metadata.put("request_id", requestId);
            metadata.put("client_ip", clientIp);
            metadata.put("user_agent", userAgent);
            metadata.put("query_string", request.getQueryString());
            metadata.put("content_type", response.getContentType());
            
            String message = String.format("Request %s %s completed with status %d in %dms", 
                request.getMethod(), 
                request.getRequestURI(),
                status,
                duration);
            
            LoggingUtils.logOperation(log, message, metadata);
        }
    }
}
