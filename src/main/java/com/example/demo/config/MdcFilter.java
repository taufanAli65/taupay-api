package com.example.demo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcFilter extends OncePerRequestFilter {

    private static final String REQ_ID_KEY = "reqId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String reqId = request.getHeader("X-Request-ID");
        if (reqId == null || reqId.isBlank()) {
            reqId = UUID.randomUUID().toString();
        }

        try {
            MDC.put(REQ_ID_KEY, reqId);
            response.setHeader("X-Request-ID", reqId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
