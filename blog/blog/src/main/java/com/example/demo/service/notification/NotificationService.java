package com.example.demo.service.notification;

import com.example.demo.entity.member.MemberEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface NotificationService {
    SseEmitter subscribe(Long memberId);

    void send(MemberEntity boardWriter, String content, String url);
}
