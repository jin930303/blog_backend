package com.example.demo.dto.member.google;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GoogleUserInfoDTO {

    private String id; //provideId
    private String nickname;
    private String email;
    private String picture;

}
