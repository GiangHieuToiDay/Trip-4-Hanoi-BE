package com.trip4hanoi.app.service;



import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.trip4hanoi.app.dto.JwtInfo;
import com.trip4hanoi.app.dto.TokenPayload;
import com.trip4hanoi.app.entity.RedisToken;
import com.trip4hanoi.app.entity.Role;
import com.trip4hanoi.app.entity.User;
import com.trip4hanoi.app.repository.RedisTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j(topic = "JWTSERVICE")
@RequiredArgsConstructor
public class JwtService {


    @Value("${application.security.jwt.secret-key}")
    private String SECRET_KEY;

    private final RedisTokenRepository redisTokenRepository;


    /**
     * tạo accessToken ngắn hạn cho user
     * @param user
     * @return
     */
    public com.trip4hanoi.app.dto.TokenPayload generateAccessToken(User user){
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        Date issusTime = new Date();
        Date expiryTime = Date.from(issusTime.toInstant().plus(30, ChronoUnit.MINUTES));

        String jwtId = UUID.randomUUID().toString();

        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issueTime(issusTime)
                .expirationTime(expiryTime)
                .jwtID(jwtId)
                .claim("roles", roleNames)
                .build();
        Payload payload = new Payload(claimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(jwsHeader,payload);

        try {
            jwsObject.sign(new MACSigner(SECRET_KEY));
        } catch (KeyLengthException e) {
            throw new RuntimeException(e);
        } catch (JOSEException e) {
            log.error("JWT signing error: {}", e.getMessage());
            throw new RuntimeException("Cannot sign JWT", e);
        }

        String token = jwsObject.serialize();

        long ttlSeconds = (expiryTime.getTime() - issusTime.getTime()) / 1000;
        if(ttlSeconds >0){
            redisTokenRepository.save(RedisToken.builder()
                            .jwtId(jwtId)
                            .email(user.getEmail())
                            .expirationTime(ttlSeconds)
                    .build());
        }


        return TokenPayload.builder()
                .jwtId(jwtId)
                .token(token)
                .expirationTime(expiryTime)
                .build();
    }

    /**
     * tạo refreshToken với thời hạn dài hơn
     * @param user
     * @return
     */
    public  TokenPayload generateRefreshToken(User user){
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        Date issueTime = new Date();
        Date expirationTime = Date.from(issueTime.toInstant().plus(15, ChronoUnit.DAYS));
        String jwtId = UUID.randomUUID().toString();

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issueTime(issueTime)
                .expirationTime(expirationTime)
                .jwtID(jwtId)
                .build();

        Payload payload = new Payload(claimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(jwsHeader,payload);

        try {
            jwsObject.sign(new MACSigner(SECRET_KEY));
        } catch (JOSEException e) {
            log.error("JWT signing error: {}", e.getMessage());
            throw new RuntimeException("Cannot sign refresh token", e);
        }
        String token = jwsObject.serialize();
        long ttlSeconds = (expirationTime.getTime() - System.currentTimeMillis()) / 1000;
        redisTokenRepository.save(RedisToken.builder()
                .jwtId(jwtId)
                .email(user.getEmail())
                .expirationTime(ttlSeconds)
                .build());

        return TokenPayload.builder()
                .jwtId(jwtId)
                .token(token)
                .expirationTime(expirationTime)
                .build();
    }


    /**
     * Xác minh tính hợp lệ, ngày hết hạn và chữ ký của JWT.
     * @param token
     * @return
     * @throws ParseException
     * @throws JOSEException
     */
    public boolean verifyToken(String token) throws ParseException, JOSEException {

        SignedJWT signedJWT = SignedJWT.parse(token);


        boolean isSignatureValid = signedJWT.verify(new MACVerifier(SECRET_KEY));
        if (!isSignatureValid) {
            log.warn("Invalid JWT signature");
            return false;
        }


        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        if (expirationTime.before(new Date())) {
            log.info("JWT expired at {}", expirationTime);
            return false;
        }


        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        Optional<RedisToken> redisTokenOpt = redisTokenRepository.findById(jwtId);
        if (redisTokenOpt.isEmpty()) {
            log.info("JWT ID {} not found in Redis (revoked or logged out)", jwtId);
            return false;
        }


        RedisToken redisToken = redisTokenOpt.get();
        if (redisToken.getExpirationTime() <= 0) {
            log.info("JWT ID {} in Redis expired", jwtId);
            redisTokenRepository.deleteById(jwtId);
            return false;
        }

        log.info("JWT verified successfully for email: {}", redisToken.getEmail());
        return true;

    }

    public String extractEmail(String token) throws ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        String email = signedJWT.getJWTClaimsSet().getSubject();

        return email;

    }


    public JwtInfo parseToken(String token) throws ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        Date issueTime = signedJWT.getJWTClaimsSet().getIssueTime();
        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();


        return JwtInfo.builder()
                .jwtId(jwtId)
                .issuedTime(issueTime)
                .expiredTime(expirationTime)
                .build();
    }































}
