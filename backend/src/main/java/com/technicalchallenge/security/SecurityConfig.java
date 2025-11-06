//package com.technicalchallenge.security;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
//import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
//
//import static org.springframework.transaction.TransactionDefinition.withDefaults;
//
//@EnableWebSecurity
//@Configuration
//public class SecurityConfig {
//
//    private static final AntPathRequestMatcher H2_MATCHER = new AntPathRequestMatcher("/h2-console/**");
//
//    private static final AntPathRequestMatcher[] SWAGGER_MATCHERS = {
////            new AntPathRequestMatcher("/swagger-ui/index.html"),
//            new AntPathRequestMatcher("/swagger-ui/**"),
//            new AntPathRequestMatcher("/v3/api-docs/**")
//    };
//
//    @Bean
//    SecurityFilterChain configure(HttpSecurity http) throws Exception {
//
//        http
//                // ignore CSRF for H2 console using AntPathRequestMatcher
//                .csrf(AbstractHttpConfigurer::disable
//                )
//                // allow frames from same origin (H2 console uses frames)
//                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
//                )
//                // allow unauthenticated access to H2 console using Ant matcher
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(H2_MATCHER).permitAll()
//                        .requestMatchers(SWAGGER_MATCHERS).permitAll()
//                        .anyRequest().authenticated()
//                )
//                .formLogin(AbstractHttpConfigurer::disable);
//
//        return http.build();
//    }
//}
