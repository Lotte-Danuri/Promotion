package com.lotte.danuri.promotion.redis;

import com.lotte.danuri.promotion.filter.CustomHandlerFilterFunction;
import com.lotte.danuri.promotion.handler.PromotionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class PromotionRouter {

    private PromotionHandler promotionHandler;

    public PromotionRouter(PromotionHandler promotionHandler) {
        this.promotionHandler = promotionHandler;
    }

    @Bean
    public RouterFunction<ServerResponse> router() {
        return RouterFunctions.route()
            .GET("/join", request -> promotionHandler.addPeople(request))
            .GET("/check", request -> promotionHandler.check(request))
            .filter(new CustomHandlerFilterFunction())
            .build();
    }

}
