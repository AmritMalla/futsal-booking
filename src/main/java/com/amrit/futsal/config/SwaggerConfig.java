//package com.amrit.futsal.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.builders.RequestHandlerSelectors;
//import springfox.documentation.service.ApiInfo;
//import springfox.documentation.service.Contact;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spring.web.plugins.Docket;
//import springfox.documentation.swagger2.annotations.EnableSwagger2;
//
//import java.util.Collections;
//
//
//@Configuration
//@EnableSwagger2
//public class SwaggerConfig {
//
//    @Value("${swagger.base.url:http://localhost:8080}") // default value if not provided
//    private String baseUrl;
//
//    @Bean
//    public Docket api() {
//        return new Docket(DocumentationType.SWAGGER_2)
//                .select()
//                .apis(RequestHandlerSelectors.any())
//                .paths(PathSelectors.any())
//                .build()
//                .apiInfo(apiInfo())
//                .host(baseUrl) // Set the base URL (host)
//                .pathMapping("/api"); // Specify base path if necessary
//    }
//
//    private ApiInfo apiInfo() {
//        return new ApiInfo(
//                "My API",
//                "This is my API description",
//                "1.0",
//                "Terms of service",
//                new Contact("Futsal Booking Inc.", "www.futsal.com", "email@futsal.com"),
//                "License",
//                "www.license.com",
//                Collections.emptyList()
//        );
//    }
//}
