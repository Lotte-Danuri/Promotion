package com.lotte.danuri.promotion.scheduler;

import com.lotte.danuri.promotion.constant.Promotion;
import com.lotte.danuri.promotion.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class QueueScheduler {

    private final RedisService redisService;

    public QueueScheduler(RedisService redisService) {
        this.redisService = redisService;
    }

    @Scheduled(cron = "0 0/2 * * * ?")
    //@Scheduled(fixedDelay = 1000)
    private void checkScheduler() {
        if(redisService.validEnd()) {
            log.info("======= 프로모션이 종료되었습니다. =======");
            log.info("작업열 사이즈 = {}",redisService.getSizeOfWork(Promotion.PROMOTION));

            return;
        }
        if(redisService.getSize(Promotion.PROMOTION) >= Promotion.PROMOTION.limit) {
            redisService.move(Promotion.PROMOTION);
        }
    }
}
