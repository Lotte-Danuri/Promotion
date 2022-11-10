package com.lotte.danuri.promotion.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PromotionReqDto {

    private Long memberId;
    private Long promotionId;

}
