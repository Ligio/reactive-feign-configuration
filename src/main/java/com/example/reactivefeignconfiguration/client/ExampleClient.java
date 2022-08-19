package com.example.reactivefeignconfiguration.client;

import org.springframework.web.bind.annotation.GetMapping;
import reactivefeign.client.ReactiveHttpResponse;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;

@ReactiveFeignClient(
    name = "example-client",
    url = "localhost:8081",
    configuration = ExampleClientConfiguration.class)
public interface ExampleClient {
  @GetMapping("/healthcheck")
  Mono<ReactiveHttpResponse<Mono<Void>>> healthcheck();
}
