package com.lotte.danuri.promotion.redis;

import com.lotte.danuri.promotion.constant.Promotion;
import com.lotte.danuri.promotion.kafka.KafkaProducerService;
import com.lotte.danuri.promotion.kafka.dto.PromotionReqDto;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RedisService {

    private static final Long FIRST_ELEMENT = 0L;
    private static final Long LAST_ELEMENT = -1L;
    private static final Long PUBLISH_SIZE = 100L;
    private static final Long LAST_INDEX = 1L;

    private static final int LIMIT = 30;

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

    public Boolean addPerson(String waitKey, String memberId) {
        long time = System.currentTimeMillis();
        //String value = Thread.currentThread().getName();

        //log.info("대기열에 추가 - {} ({}초)", memberId, (int)time);
        return redisTemplate.opsForZSet().add(waitKey, memberId, time);

    }

    public Long getOrderNumber(String waitKey, String memberId) {
        if(this.promotionCount.getLimit() == 0) {
            return -1L;
        }
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
        Object people = redisTemplate.opsForZSet().popMin(promotion.waitKey).getValue();

        log.info("'{}님이 작업열로 이동되었습니다.", people);

        redisTemplate.opsForZSet().add(promotion.workKey, people, System.currentTimeMillis());

    }

    public void publish(Promotion promotion) {

        Object people = redisTemplate.opsForZSet().popMin(promotion.workKey).getValue();

        log.info("'{}'님의 쿠폰이 발급되었습니다.", people);
        //redisTemplate.opsForZSet().remove(promotion.workKey, people);

        kafkaProducerService.send("promotion-coupon-insert",
            PromotionReqDto.builder()
                .memberId(Long.parseLong((String) people))
                .promotionId(promotion.promotionId)
                .build());

        this.promotionCount.decrease();
        log.info("promotionCount = {}", this.promotionCount.getLimit());

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
