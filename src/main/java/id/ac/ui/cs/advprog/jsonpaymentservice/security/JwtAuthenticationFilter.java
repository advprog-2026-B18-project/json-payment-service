package id.ac.ui.cs.advprog.jsonpaymentservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        String token = null;

        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }

        System.out.println("test");

        if (token != null && jwtUtil.validateToken(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            // TODO: handle case account banned 
            // UserDetails userDetails = userDetailsService.loadUserById(accountId);

            // if (userDetails instanceof CustomUserDetails custom && !custom.isEnabled()) {
            //     throw new AccessDeniedException("Account is banned");
            // }

            // Extracting using your new methods
            String userId = jwtUtil.getAccountIdFromToken(token);
            String email = jwtUtil.getEmailFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);

            System.out.println(userId + email + role);

            // Set the user_id as a request attribute so the Controller can grab it easily
            request.setAttribute("X-User-Id", userId);
            request.setAttribute("X-Email", email);
            request.setAttribute("X-Role", role);

            // Setting up Spring Security Context
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    email, null, authorities
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}