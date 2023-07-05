package com.jshan.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Naver Client 를 위한 Configuration Properties
 */
@Setter
@Getter
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "clients.naver")
public class NaverClientProperties {

    /**
     * Naver Client URI
     */
    private String uri;

    /**
     * Naver Client ID
     */
    private String clientId;

    /**
     * Naver Client Secret Key
     */
    private String clientSecret;
}
