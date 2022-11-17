package com.lotte.danuri.promotion.redis;

import com.lotte.danuri.promotion.filter.CustomHandlerFilterFunction;
import com.lotte.danuri.promotion.handler.PromotionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@CrossOrigin("*")
@Slf4j
public class PromotionRouter {

    private PromotionHandler promotionHandler;

    public PromotionRouter(PromotionHandler promotionHandler) {
        this.promotionHandler = promotionHandler;
    }

    @Bean
    @CrossOrigin("*")
    public RouterFunction<ServerResponse> router() {
        return RouterFunctions.route()
            .GET("/join", request -> promotionHandler.addPeople(request))
            .GET("/check", request -> promotionHandler.check(request))
            .GET("/set", request -> promotionHandler.setCount(request))
            .filter(new CustomHandlerFilterFunction())
            .build();
    }

}
