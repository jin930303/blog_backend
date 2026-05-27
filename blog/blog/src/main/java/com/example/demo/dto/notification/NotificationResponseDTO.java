package com.example.demo.dto.notification;

import com.example.demo.entity.notification.NotificationEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponseDTO {

    private Long notificationId;
    private String content;
    private String url;
    private boolean isRead;
    private LocalDateTime createdAt;

    public static NotificationResponseDTO from(NotificationEntity entity){
        return NotificationResponseDTO.builder()
                .notificationId(entity.getId())
                .content(entity.getContent())
                .url(entity.getUrl())
                .isRead(entity.isRead())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
