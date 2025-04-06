package com.freewheel.FreeWheelBackend.seguridad;

import com.freewheel.FreeWheelBackend.persistencia.dtos.UserDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    private Key getSigningKey() { //Convierte la secret en una clave segura de tipo Key para firmar los JWTs
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    //Generar el token
    public String generateToken(UserDTO userDTO) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userDTO.getId());
        claims.put("nombre", userDTO.getNombre());
        claims.put("correo", userDTO.getCorreo());

        return createToken(claims, userDTO.getApellido());
    }

    //Construir el token:
    public String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public Boolean validateToken(String token, String correo) {
        final String extractedEmail = extractEmail(token);
        return extractedEmail.equals(correo);
    }

    public String extractEmail(String token) {
        return extractClaim(token, (claims -> claims.getSubject()));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, (claims) -> claims.getExpiration());
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

}
