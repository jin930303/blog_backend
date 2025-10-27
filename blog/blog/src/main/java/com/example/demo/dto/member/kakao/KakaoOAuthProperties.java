package com.example.demo.dto.member.kakao;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter @Setter
@Component
@ConfigurationProperties(prefix = "kakao.oauth")
public class KakaoOAuthProperties {
    private String clientId;
    private String redirectUri;
    private String tokenUri;
    private String userInfoUri;
}
