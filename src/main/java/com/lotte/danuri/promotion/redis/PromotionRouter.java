package com.lotte.danuri.promotion.redis;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import com.lotte.danuri.promotion.handler.PromotionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.function.BodyInserters;
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
            .build();
    }

}
