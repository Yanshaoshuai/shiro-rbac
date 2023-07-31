package com.rbac.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;

public class JWTUtil {

    public static KeyPair getKeyPair(SignatureAlgorithm algorithm) {
        return Keys.keyPairFor(algorithm);
    }

    /**
     * 使用私钥签发token
     * 用于授权模块
     *
     * @param username
     * @param expireMills
     * @param algorithm
     * @param privateKey
     * @return
     */
    public static String getToken(String username, long expireMills, SignatureAlgorithm algorithm, PrivateKey privateKey,String id) {
        Claims claims = Jwts.claims().setSubject(username);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expireMills))
                .signWith(privateKey, algorithm)
                .setId(id)
                .setIssuer("")
                .compact();
    }

    /**
     * 使用公钥验证token
     * 用于业务模块
     *
     * @param token
     * @param publicKey
     * @return
     */
    public static Jws<Claims> getClaims(String token, PublicKey publicKey) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token);
    }

    public static Header getHeader(String token, PublicKey publicKey) {
        Jws<Claims> claims = getClaims(token, publicKey);
        return claims.getHeader();
    }

    public static Claims getBody(String token, PublicKey publicKey) {
        Jws<Claims> claims = getClaims(token, publicKey);
        return claims.getBody();
    }

    public static String getSignature(String token, PublicKey publicKey) {
        Jws<Claims> claims = getClaims(token, publicKey);
        return claims.getSignature();
    }

    public static void main(String[] args) {
        KeyPair keyPair = getKeyPair(SignatureAlgorithm.RS512);
        System.out.println("private key is:\n"+ Base64.getMimeEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
        System.out.println("public key is:\n"+ Base64.getMimeEncoder().encodeToString(keyPair.getPublic().getEncoded()));
    }

}
