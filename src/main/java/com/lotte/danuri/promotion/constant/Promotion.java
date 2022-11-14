package com.lotte.danuri.promotion.constant;

import lombok.Getter;

@Getter
public enum Promotion {

    PROMOTION("COUPON", "COUPON_WAIT", "COUPON_WORK", 50, 1L);

    public String name;
    public String waitKey;
    public String workKey;
    public int limit;
    public Long promotionId;

    Promotion(String name, String waitKey, String workKey, int limit, Long promotionId) {
        this.name = name;
        this.waitKey = waitKey;
        this.workKey = workKey;
        this.limit = limit;
        this.promotionId = promotionId;
    }
}
