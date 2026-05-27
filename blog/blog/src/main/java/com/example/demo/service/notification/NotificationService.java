package com.example.demo.service.notification;

import com.example.demo.dto.notification.NotificationResponseDTO;
import com.example.demo.entity.member.MemberEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface NotificationService {
    SseEmitter subscribe(Long memberId);

    void send(MemberEntity boardWriter, String content, String url);

    List<NotificationResponseDTO> getNotifications(Long memberId);

    long getUnreadCount(Long memberId);

    void markAsRead(Long notificationId,Long memberId);

    void markAsAllRead(Long memberId);

    void delete(Long notificationId, Long memberId);


    List<NotificationResponseDTO> getUnreadNotifications(Long memberId);
}
