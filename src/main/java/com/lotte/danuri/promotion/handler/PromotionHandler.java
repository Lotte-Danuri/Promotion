package com.lotte.danuri.promotion.handler;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import com.lotte.danuri.promotion.constant.Promotion;
import com.lotte.danuri.promotion.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class PromotionHandler {
    private final RedisService redisService;

    public PromotionHandler(RedisService redisService) {
        this.redisService = redisService;
    }

    public Mono<ServerResponse> addPeople(ServerRequest request) {
        String memberId = request.headers().header("memberId").get(0);
        //String promotionId = request.headers().header("promotionId").get(0);

        Boolean result = redisService.addPerson(Promotion.PROMOTION.waitKey, memberId);
        if(result == null) {
            result = false;
        }

        return ok().bodyValue(result);
    }

    public Mono<ServerResponse> check(ServerRequest request) {
        String memberId = request.headers().header("memberId").get(0);
        //String promotionId = request.headers().header("promotionId").get(0);

        // -1 : 프로모션 종료, null : 대기열 존재 안함, 작업열 존재 -> 쿠폰 받은 거 성공
        Long rank = redisService.getOrderNumber(Promotion.PROMOTION.waitKey, memberId);
        if(rank == null) { // 대기열 존재 안할 때 -> 1. 작업열로 이동, 2. 대기열에 진입 조차 못함
            Long workRank = redisService.getWorkNumber(Promotion.PROMOTION.workKey, memberId);

            if(workRank != null) {
                String msg = "프로모션 응모에 성공하셨습니다!";
                return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(msg);
            }

            rank = -100L;

        }

        return ok().contentType(MediaType.APPLICATION_JSON).bodyValue(rank);

    }

}