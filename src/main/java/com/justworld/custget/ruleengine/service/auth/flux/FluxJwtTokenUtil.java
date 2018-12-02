package com.justworld.custget.ruleengine.service.auth.flux;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class FluxJwtTokenUtil {
    public static final String TOKEN_PREFIX = "Bearer ";

    @Value("${jwt_token.secret}")
    private String secret;
    @Value("${jwt_token.expire_second}")
    private int expireSecond;
    @Value("${jwt_token.remember_me_expire_second}")
    private int rememberMeExpireSecond;

    public static Map<String, UserDetails> validTokenStore = new HashMap<>();
    public static Map<String, Set<String>> loginedUserStore = new HashMap<>();

/*
    private static InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("jwt.jks"); // 寻找证书文件
    private static PrivateKey privateKey = null;
    private static PublicKey publicKey = null;

    static { // 将证书文件里边的私钥公钥拿出来
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS"); // java key store 固定常量
            keyStore.load(inputStream, "123456".toCharArray());
            privateKey = (PrivateKey) keyStore.getKey("jwt", "123456".toCharArray());// jwt 为 命令生成整数文件时的别名
            publicKey = keyStore.getCertificate("jwt").getPublicKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/

    public String generateToken(String subject) {
        return Jwts.builder()
                .setClaims(null)
                .setSubject(subject)
                .setExpiration(new Date(System.currentTimeMillis() + expireSecond * 1000)) //
                .signWith(SignatureAlgorithm.HS512, secret) // 不使用公钥私钥
//                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();
    }

    public String getUsername(String token) throws ExpiredJwtException {
        return getTokenBody(token).getSubject();
    }

    public Claims getTokenBody(String token) throws ExpiredJwtException {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    public String getToken(ServerHttpRequest request){
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(FluxJwtTokenUtil.TOKEN_PREFIX)) {
            return authHeader.substring(FluxJwtTokenUtil.TOKEN_PREFIX.length());
        }
        return "";
    }
}
