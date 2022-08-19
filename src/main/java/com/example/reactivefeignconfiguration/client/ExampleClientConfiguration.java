package com.example.reactivefeignconfiguration.client;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactivefeign.retry.BasicReactiveRetryPolicy;
import reactivefeign.retry.ReactiveRetryPolicy;
import reactivefeign.spring.config.EnableReactiveFeignClients;

@Configuration
@EnableReactiveFeignClients(clients = {ExampleClient.class})
public class ExampleClientConfiguration {
  @Bean
  public ReactiveRetryPolicy retryer() {
    return BasicReactiveRetryPolicy.retry(3);
  }

  @Bean
  public ErrorDecoder errorDecoder() {
    return new ErrorDecoder.Default();
  }
}
