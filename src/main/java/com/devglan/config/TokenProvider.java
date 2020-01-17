package com.devglan.config;

import io.jsonwebtoken.*;

import org.hibernate.mapping.SingleTableSubclass;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.devglan.model.Constants.ACCESS_TOKEN_VALIDITY_SECONDS;
import static com.devglan.model.Constants.AUTHORITIES_KEY;

@Component
public class TokenProvider implements Serializable {
    private PrivateKey privateKey;
    private PublicKey publicKey;

    //Try/catches need cleanup
    public TokenProvider() {
        super();
        byte[] privateKeyBytes = new byte[0];
        byte[] publicKeyBytes = new byte[0];
        try {
            //TODO Add Config
            publicKeyBytes = Files.readAllBytes(
                    Paths.get("/Users/michael.goldstein/src/Spring-Boot-With-JWT-token-example/public_key.der"));
            privateKeyBytes = Files.readAllBytes(
                    Paths.get("/Users/michael.goldstein/src/Spring-Boot-With-JWT-token-example/private_key.der"));
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyBytes);
        final X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(publicKeyBytes);
        try {
            final KeyFactory kf = KeyFactory.getInstance("RSA");
            privateKey = kf.generatePrivate(spec);
            publicKey = kf.generatePublic(pubSpec);
        } catch (final Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public String getUsernameFromToken(final String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(final String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(final String token, final Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(final String token) {
        return Jwts.parser()
                .setSigningKey(publicKey)
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(final String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public String generateToken(final Authentication authentication) {
        final String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY_SECONDS*1000))
                .compact();
    }

    public String updateToken(final String token){
        final JwtParser jwtParser = Jwts.parser().setSigningKey(publicKey);
        final Jws<Claims> claimsJws = jwtParser.parseClaimsJws(token);
        final Claims claims = claimsJws.getBody();
        
        return Jwts.builder()
            .setClaims(claims)
            .signWith(SignatureAlgorithm.RS256, privateKey)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY_SECONDS*1000))
            .compact(); 

    }

    public String generateRefresh(){
        return Jwts.builder()
        .setSubject("refresh")
        .signWith(SignatureAlgorithm.RS256, privateKey)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY_SECONDS*5000))
        .compact();
    }

    public Boolean validateToken(final String token) {
        return (!isTokenExpired(token));
    }

    UsernamePasswordAuthenticationToken getAuthentication(final String token, final Authentication existingAuth, final String userName) {

        final JwtParser jwtParser = Jwts.parser().setSigningKey(publicKey);

        final Jws<Claims> claimsJws = jwtParser.parseClaimsJws(token);

        final Claims claims = claimsJws.getBody();

        final Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
        final UserDetails userDetails = new org.springframework.security.core.userdetails.User(userName, "pass", authorities);
        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }
}
