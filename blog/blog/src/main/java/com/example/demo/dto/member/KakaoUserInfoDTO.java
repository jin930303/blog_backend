package com.example.demo.dto.member;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor(force = true) // 기본 생성자( 빌더 패턴 쓸 때 권장 )
@AllArgsConstructor // 모든 필드를 인수로 받는 생성자 ( 빌더랑 같이 사용댐)
public class KakaoUserInfoDTO {
    private final Long id;
    private final String nickname;
    private final String profile_image;
    private final String email;
}
