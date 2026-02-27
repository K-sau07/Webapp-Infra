package com.csye6225.webapp.config;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class RequestMetricsFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestMetricsFilter.class);
    private final MeterRegistry meterRegistry;

    public RequestMetricsFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        long start = System.currentTimeMillis();

        logger.info("Received {} request to {}", request.getMethod(), request.getRequestURI());

        chain.doFilter(req, res);

        long duration = System.currentTimeMillis() - start;
        String routeName = request.getMethod() + "_" + request.getRequestURI().replace("/", "_");
        meterRegistry.counter("api." + routeName + ".count").increment();
        meterRegistry.timer("api." + routeName + ".duration").record(duration, TimeUnit.MILLISECONDS);
    }
}
