package com.freewheel.FreeWheelBackend.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.util.List;
import java.util.Arrays;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");
        String uri = request.getRequestURI();

        // Lista de rutas públicas que no requieren validación de token
        List<String> publicPaths = Arrays.asList(
                "/usuarios/", "/auth/", "/vehiculos/", "/viajes/", "/conductores/", "/pasajeros/"
        );

        // Si es una ruta pública, permitir el acceso sin validar token
        boolean isPublicPath = publicPaths.stream().anyMatch(path -> uri.startsWith(path));
        if (isPublicPath) {
            System.out.println("Ruta pública detectada: " + uri + " - Se omite validación de token");
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("========== JWT FILTER DEBUG ==========");
        System.out.println("URI solicitada: " + uri);
        System.out.println("Token recibido: " + authorizationHeader);

        String correo = null;
        String jwt = null;

        // Extrae el token de los headers de la request
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            System.out.println("JWT extraído: " + jwt);

            try {
                correo = jwtUtils.extractEmail(jwt);
                System.out.println("Correo extraído del token: " + correo);
            } catch (Exception e) {
                System.out.println("Error al extraer correo del token: " + e.getMessage());
            }
        } else {
            System.out.println("No se recibió token o formato incorrecto");
        }

        if (correo != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(correo);
                System.out.println("UserDetails cargado correctamente para: " + correo);

                boolean tokenValido = jwtUtils.validateToken(jwt, correo);
                System.out.println("¿Token válido? " + tokenValido);

                if (tokenValido) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("Autenticación establecida correctamente en el contexto de seguridad");
                }
            } catch (Exception e) {
                System.out.println("Error durante la autenticación: " + e.getMessage());
            }
        }

        System.out.println("======================================");
        filterChain.doFilter(request, response);
    }
}