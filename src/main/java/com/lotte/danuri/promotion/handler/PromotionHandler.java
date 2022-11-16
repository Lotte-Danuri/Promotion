package com.lotte.danuri.promotion.handler;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import com.lotte.danuri.promotion.constant.Promotion;
import com.lotte.danuri.promotion.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@CrossOrigin("*")
public class PromotionHandler {
    private final RedisService redisService;

    public PromotionHandler(RedisService redisService) {
        this.redisService = redisService;
    }

    public Mono<ServerResponse> addPeople(ServerRequest request) {
        String memberId = request.headers().header("memberId").get(0);

        Boolean result = redisService.addPerson(Promotion.PROMOTION.waitKey, memberId);
        if(result == null) {
            result = false;
        }

        return ok().bodyValue(result);
    }

    public Mono<ServerResponse> check(ServerRequest request) {
        String memberId = request.headers().header("memberId").get(0);

        if(redisService.validEnd()) {
            String msg = "exited";
            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(msg);
        }

        Long rank = redisService.getOrderNumber(Promotion.PROMOTION.waitKey, memberId);

        if(rank == null) {
            Long workRank = redisService.getOrderNumber(Promotion.PROMOTION.workKey, memberId);

            if(workRank == null) {
                String msg = "waiting";
                return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(msg);
            }

            if(workRank <= Promotion.PROMOTION.limit) {
                String msg = "success";
                return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(msg);
            }

            rank = workRank;

        }

        return ok().contentType(MediaType.APPLICATION_JSON).bodyValue(rank);

    }

    public Mono<ServerResponse> setCount() {
        log.info("Call setCount");
        redisService.setPromotionCount(Promotion.PROMOTION.limit);
        int count = redisService.getPromotionCount();
        return ok().bodyValue(count);
    }

}
