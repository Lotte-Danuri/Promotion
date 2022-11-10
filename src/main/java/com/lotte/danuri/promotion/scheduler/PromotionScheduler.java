package com.lotte.danuri.promotion.scheduler;

import com.lotte.danuri.promotion.constant.Promotion;
import com.lotte.danuri.promotion.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PromotionScheduler {

    private final RedisService redisService;

    public PromotionScheduler(RedisService redisService) {
        this.redisService = redisService;
    }

    @Scheduled(cron = "0 6 20 * * ?")
    private void promotionScheduler() {
        if(redisService.getSizeOfWork(Promotion.PROMOTION) > 0) {
            redisService.publish(Promotion.PROMOTION);
        }

        redisService.delete(Promotion.PROMOTION);
        redisService.setPromotionCount(Promotion.PROMOTION.limit);
    }
}
