package com.lotte.danuri.promotion.kafka;

import com.lotte.danuri.promotion.kafka.dto.PromotionReqDto;

public interface KafkaProducerService {

    PromotionReqDto send(String topic, PromotionReqDto promotionReqDto);

}
