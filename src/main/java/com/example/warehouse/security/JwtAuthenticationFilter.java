package com.example.warehouse.security;

import com.example.warehouse.enums.ErrorCodeEnum;
import com.example.warehouse.payload.response.ApiResponse;
import com.example.warehouse.repository.UserRepository;
import com.example.warehouse.utility.ResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String jwt = getJwtFromRequest(request);

        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String username = jwtService.extractUsername(jwt);

            // If the token is valid, configure Spring Security to manually set authentication
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // The user lookup logic is now performed directly in the filter.
                UserDetails userDetails = this.userRepository.findByUsername(username)
                        .map(SecurityUser::new)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // Credentials are not needed as we are using JWT
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // After setting the Authentication in the context, we specify
                    // that the current user is authenticated. So it passes the Spring Security Configurations successfully.
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (ExpiredJwtException e) {
            handleJwtException(request, response,
                    ErrorCodeEnum.AUTH_TOKEN_EXPIRED, "Your session has expired. Please login again.");
            return;
        } catch (MalformedJwtException e) {
            handleJwtException(request, response,
                    ErrorCodeEnum.AUTH_TOKEN_MALFORMED, "Invalid token format.");
            return;
        } catch (SignatureException | SecurityException e) {
            handleJwtException(request, response,
                    ErrorCodeEnum.AUTH_TOKEN_INVALID, "Invalid token signature.");
            return;
        } catch (UsernameNotFoundException e) {
            handleJwtException(request, response,
                    ErrorCodeEnum.USER_NOT_FOUND, "User not found.");
            return;
        } catch (Exception e) {
            handleJwtException(request, response,
                    ErrorCodeEnum.UNAUTHORIZED_ACCESS, "Authentication failed.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void handleJwtException(HttpServletRequest request,
                                    HttpServletResponse response,
                                    ErrorCodeEnum errorCode,
                                    String message) throws IOException {

        // Sử dụng ResponseUtil để tạo error response
        ResponseEntity<ApiResponse<Object>> responseEntity = ResponseUtil.createErrorResponse(
                HttpStatus.UNAUTHORIZED, errorCode, message, request.getRequestURI()
        );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = objectMapper.writeValueAsString(responseEntity.getBody());
        response.getWriter().write(jsonResponse);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        final String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
