package com.inspectron.inspectron.infra.security;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {

    private String secret;
    private String assetsSecret;
    private String firstAccessSecret;
    private Duration accessTokenExpiration = Duration.ofDays(1);
    private Duration rememberMeAccessTokenExpiration = Duration.ofDays(7);
    private Duration firstAccessTokenExpiration = Duration.ofMinutes(10);
}
