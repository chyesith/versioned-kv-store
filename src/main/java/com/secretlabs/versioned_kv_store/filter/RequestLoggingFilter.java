package com.secretlabs.versioned_kv_store.filter;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)
public class RequestLoggingFilter implements Filter {

    private static final String REQUEST_ID = "requestId";


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse
            servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;

        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(REQUEST_ID, requestId);
        res.setHeader("X-Request-Id", requestId);

        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            long duration = System.currentTimeMillis() - start;
            MDC.put("duration", duration + "ms");
            MDC.clear();
        }
    }
}
