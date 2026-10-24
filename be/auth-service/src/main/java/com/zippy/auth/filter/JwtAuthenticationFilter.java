package com.zippy.auth.filter;
import com.zippy.auth.security.*;
import com.zippy.auth.constant.SecurityConstants;
import com.zippy.auth.util.BearerTokenUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final CustomUserDetailsService users;
    private final RestAuthenticationEntryPoint entryPoint;
    private final TokenBlacklist blacklist;
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = BearerTokenUtils.extract(request.getHeader(SecurityConstants.AUTHORIZATION_HEADER));
        if (token != null) {
            try {
                var decoded = jwt.decode(token);
                if (blacklist.isRevoked(token)) throw new BadCredentialsException("Revoked token");
                if (blacklist.isSessionRevoked(jwt.sessionId(decoded))) throw new BadCredentialsException("Revoked session");
                UserPrincipal user = users.loadById(java.util.UUID.fromString(decoded.getSubject()));
                if (!user.isEnabled()) throw new DisabledException("Account unavailable");
                if (!jwt.matchesPassword(decoded, user.getPassword())) throw new BadCredentialsException("Credentials changed");
                var context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities()));
                SecurityContextHolder.setContext(context);
            } catch (JwtException | IllegalArgumentException | AuthenticationException ex) {
                SecurityContextHolder.clearContext();
                entryPoint.commence(request, response, new BadCredentialsException("Invalid token"));
                return;
            } catch (org.springframework.dao.DataAccessException ex) {
                SecurityContextHolder.clearContext();
                response.setStatus(503);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"message\":\"Authentication service unavailable\",\"errorCode\":\"AUTH_DEPENDENCY_UNAVAILABLE\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
