package com.example.reactivefeignconfiguration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactivefeign.spring.config.EnableReactiveFeignClients;

@SpringBootApplication
public class ReactiveFeignConfigurationApplication {

  public static void main(String[] args) {
    SpringApplication.run(ReactiveFeignConfigurationApplication.class, args);
  }

}
