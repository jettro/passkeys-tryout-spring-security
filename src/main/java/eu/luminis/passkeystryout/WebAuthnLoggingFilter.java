package eu.luminis.passkeystryout;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class WebAuthnLoggingFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(WebAuthnLoggingFilter.class);
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (request instanceof HttpServletRequest httpRequest) {
            String requestURI = httpRequest.getRequestURI();
            
            if (requestURI.contains("/webauthn") || requestURI.contains("/login/webauthn")) {
                ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest, 1024 * 10);
                
                logger.info("WebAuthn Request: {} {}", httpRequest.getMethod(), requestURI);
                logger.info("Content-Type: {}", httpRequest.getContentType());
                
                chain.doFilter(wrappedRequest, response);
                
                byte[] content = wrappedRequest.getContentAsByteArray();
                if (content.length > 0) {
                    String body = new String(content, StandardCharsets.UTF_8);
                    logger.info("Request Body: {}", body);
                }
                
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
}
