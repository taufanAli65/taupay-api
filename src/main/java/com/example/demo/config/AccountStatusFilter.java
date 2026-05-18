package com.example.demo.config;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.dtos.responses.BaseResponse;
import com.example.demo.entities.AccountEntity;
import com.example.demo.exceptions.AccountLockedException;
import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.services.AccountAccessService;
import com.example.demo.utils.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AccountStatusFilter extends OncePerRequestFilter {

    private final AccountRepository accountRepository;
    private final AccountAccessService accountAccessService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null || email.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        AccountEntity account = accountRepository.findByEmail(email).orElse(null);
        if (account == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            accountAccessService.assertCanLogin(account);
            filterChain.doFilter(request, response);
        } catch (AccountLockedException ex) {
            writeError(response, HttpStatus.LOCKED, ex.getMessage());
        } catch (UnauthorizedException ex) {
            writeError(response, HttpStatus.UNAUTHORIZED, ex.getMessage());
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/v1/auth")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.equals("/swagger-ui.html");
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        objectMapper.writeValue(response.getOutputStream(), BaseResponse.error(message, null));
    }
}
