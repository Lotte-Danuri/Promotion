package com.lotte.danuri.promotion.redis;

import com.lotte.danuri.promotion.constant.Promotion;
import com.lotte.danuri.promotion.kafka.KafkaProducerService;
import com.lotte.danuri.promotion.kafka.dto.PromotionReqDto;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RedisService {

    private static final int LIMIT = Promotion.PROMOTION.limit;
    private Long publishSize = 10L;
    private static final Long LAST_INDEX = 1L;

    private PromotionCount promotionCount = new PromotionCount(LIMIT);
    private StringBuilder sb = new StringBuilder();

    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaProducerService kafkaProducerService;

    public RedisService(RedisTemplate<String, Object> redisTemplate,
        KafkaProducerService kafkaProducerService) {
        this.redisTemplate = redisTemplate;
        this.kafkaProducerService = kafkaProducerService;
    }

    public void setPromotionCount(int queue) {
        this.promotionCount = new PromotionCount(queue);
    }
    public int getPromotionCount() {
        return this.promotionCount.getLimit();
    }
    public void setPublishSize(Long size) {
        this.publishSize = size;
    }

    public Boolean addPerson(String waitKey, String memberId) {
        long time = System.currentTimeMillis();
        //String value = Thread.currentThread().getName();

        //log.info("대기열에 추가 - {} ({}초)", memberId, (int)time);
        return redisTemplate.opsForZSet().add(waitKey, memberId, time);

    }

    public Long getOrderNumber(String waitKey, String memberId) {
        Long rank = redisTemplate.opsForZSet().rank(waitKey, memberId);
        //log.info("'{}'님의 현재 대기열은 {}명 남았습니다.", memberId, rank);

        return rank;
    }

    public Long getWorkNumber(String workKey, String memberId) {
        Long rank = redisTemplate.opsForZSet().rank(workKey, memberId);
        //log.info("{}님의 작업열 위치는 {}등 입니다.", memberId, rank);

        return rank;
    }

    public void move(Promotion promotion) {
        /*Object people = redisTemplate.opsForZSet().popMin(promotion.waitKey).getValue();

        //log.info("'{}님이 작업열로 이동되었습니다.", people);

        redisTemplate.opsForZSet().add(promotion.workKey, people, System.currentTimeMillis());
        this.promotionCount.decrease();*/

        final long start = 0L;
        final long end = publishSize - LAST_INDEX;

        Set<Object> queue = redisTemplate.opsForZSet().range(promotion.waitKey, start, end);
        queue.forEach(people -> {
            if(validEnd()) {
                return;
            }
            redisTemplate.opsForZSet().remove(promotion.waitKey, people);

            redisTemplate.opsForZSet().add(promotion.workKey, people, System.currentTimeMillis());

            this.promotionCount.decrease();
            log.info("{}님 이동, promotionCount = {}", people, this.promotionCount.getLimit());
        });

    }

    public void publish(Promotion promotion) {

        /*Object people = redisTemplate.opsForZSet().popMin(promotion.workKey).getValue();

        //log.info("'{}'님의 쿠폰이 발급되었습니다.", people);
        //redisTemplate.opsForZSet().remove(promotion.workKey, people);

        this.promotionCount.decrease();
        log.info("promotionCount = {}", this.promotionCount.getLimit());

        kafkaProducerService.send("promotion-coupon-insert",
            PromotionReqDto.builder()
                .memberId(Long.parseLong((String) people))
                .promotionId(promotion.promotionId)
                .build());*/

        final long start = 0L;

        Set<Object> queue = redisTemplate.opsForZSet().range(promotion.workKey, start, LIMIT);
        queue.forEach(people -> {
            log.info("'{}'님의 쿠폰이 발급되었습니다.", people);
            redisTemplate.opsForZSet().remove(promotion.workKey, people);

            kafkaProducerService.send("promotion-coupon-insert",
                PromotionReqDto.builder()
                    .memberId(Long.parseLong((String) people))
                    .promotionId(promotion.promotionId)
                    .build());

        });

    }

    public boolean validEnd() {
        return this.promotionCount != null && this.promotionCount.end();
    }

    public Long getSize(Promotion promotion) {
        return redisTemplate.opsForZSet().size(promotion.waitKey);
    }

    public Long getSizeOfWork(Promotion promotion) {
        return redisTemplate.opsForZSet().size(promotion.workKey);
    }

    public void delete(Promotion promotion) {
        log.info("Call delete method");
        redisTemplate.delete(promotion.waitKey);
        redisTemplate.delete(promotion.workKey);
    }
}
