package com.jshan.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Kakao Client 를 위한 Configuration Properties
 */
@Setter
@Getter
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "clients.kakao")
public class KakaoClientProperties {

    /**
     * Kakao Client URI
     */
    private String uri;

    /**
     * Kakao Client API Key
     */
    private String apiKey;
}
