package com.lotte.danuri.promotion;

import static org.assertj.core.api.Assertions.assertThat;

import com.lotte.danuri.promotion.constant.Promotion;
import com.lotte.danuri.promotion.redis.RedisService;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class RedisServiceTest {

    @Autowired
    RedisService redisService;

    @Test
    void 프로모션_테스트() throws InterruptedException {
        final Promotion couponPromotion = Promotion.PROMOTION;
        final int people = 2000;
        final int limit = 200;
        final CountDownLatch countDownLatch = new CountDownLatch(people);

        redisService.delete(couponPromotion);

        redisService.setPromotionCount(limit);

        List<Thread> workers = Stream
                                .generate(() -> new Thread(new AddQueueWorker(countDownLatch, couponPromotion)))
                                .limit(people).toList();

        workers.forEach(Thread::start);
        countDownLatch.await();
        Thread.sleep(5000);

        final long failEventPeople = redisService.getSize(couponPromotion);
        assertThat(people - limit).isEqualTo(failEventPeople);
    }

    private class AddQueueWorker implements Runnable {

        private CountDownLatch countDownLatch;
        private Promotion promotion;

        public AddQueueWorker(CountDownLatch countDownLatch, Promotion promotion) {
            this.countDownLatch = countDownLatch;
            this.promotion = promotion;
        }

        @Override
        public void run() {
            redisService.add(promotion);
            countDownLatch.countDown();
        }
    }
}
