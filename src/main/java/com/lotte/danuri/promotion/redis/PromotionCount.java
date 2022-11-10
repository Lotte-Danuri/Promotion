package com.lotte.danuri.promotion.redis;

import lombok.Getter;

@Getter
public class PromotionCount {

    private int limit;

    private static final int END = 0;

    public PromotionCount(int limit) {
        this.limit = limit;
    }

    public synchronized void decrease() {
        this.limit--;
    }

    public boolean end() {
        return this.limit <= END;
    }
}
