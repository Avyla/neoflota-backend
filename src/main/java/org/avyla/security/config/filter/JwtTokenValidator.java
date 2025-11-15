package org.avyla.security.config.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.avyla.shared.util.JwtUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;  // ← AGREGAR ESTE IMPORT
import org.springframework.security.core.userdetails.UserDetails;  // ← AGREGAR ESTE IMPORT
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

@Component
@RequiredArgsConstructor
public class JwtTokenValidator extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException
    {
        String jwtToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (jwtToken != null)
        {
            // Bearer <token> -> se extrae el token
            jwtToken = jwtToken.substring(7);

            DecodedJWT decodedJWT = jwtUtils.validateToken(jwtToken);

            String username = jwtUtils.extractUsername(decodedJWT);
            String stringAuthorities = jwtUtils.getSpecificClaim(decodedJWT, "authorities").asString();

            Collection<? extends GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(stringAuthorities);

            // ✅ CAMBIO: Crear un UserDetails en lugar de pasar solo el username
            UserDetails userDetails = User.builder()
                    .username(username)
                    .password("") // No se necesita password para JWT
                    .authorities(authorities)
                    .build();

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            // ✅ CAMBIO: Pasar userDetails como principal en lugar de username
            Authentication authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
            context.setAuthentication(authenticationToken);
            SecurityContextHolder.setContext(context);
        }

        filterChain.doFilter(request, response);
    }
}