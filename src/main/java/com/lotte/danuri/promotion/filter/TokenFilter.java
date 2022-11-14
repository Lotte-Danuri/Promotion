package com.lotte.danuri.promotion.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.AuthorizationException;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@CrossOrigin("*")
@Slf4j
public class TokenFilter implements WebFilter {

    Environment env;

    public TokenFilter(Environment env) {
        this.env = env;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            return Mono.error(new AuthorizationException("No Authorization Header"));
        }

        String token = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
        String jwt = token.replace("Bearer ", "");
        //log.info("Request Token = {}",jwt);

        if(!isJwtValid(jwt)) {
            return Mono.error(new AuthorizationException("JWT token is not valid"));
        }

        if(!validateTokenExceptionExpiration(jwt)) {
            return Mono.error(new AuthorizationException("JWT Access Token is expired"));
        }

        String memberId = Jwts.parser().setSigningKey(env.getProperty("token.secret"))
            .parseClaimsJws(jwt).getBody().getSubject();
        //log.info("parsing memberId = {}", memberId);

        ServerHttpRequest newRequest = request.mutate()
            .header("memberId", memberId).build();

        ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();

        return chain.filter(newExchange);
    }

    private boolean isJwtValid(String jwt) {
        boolean returnValue = true;

        String subject = null;

        try {
            subject = Jwts.parser().setSigningKey(env.getProperty("token.secret"))
                .parseClaimsJws(jwt).getBody()
                .getSubject();
        } catch (Exception e) {
            returnValue = false;
        }

        if(subject == null || subject.isEmpty()) {
            returnValue = false;
        }

        return returnValue;
    }

    public boolean validateTokenExceptionExpiration(String token) {

        Jws<Claims> claims = Jwts.parser().setSigningKey(env.getProperty("token.secret"))
            .parseClaimsJws(token);

        return claims.getBody().getExpiration().after(new Date());

    }
}
