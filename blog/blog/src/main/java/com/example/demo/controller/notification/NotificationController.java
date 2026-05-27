package com.example.demo.controller.notification;

import com.example.demo.dto.notification.NotificationResponseDTO;
import com.example.demo.service.member.CustomUserDetails;
import com.example.demo.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor

public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(value = "/subscribe",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal CustomUserDetails userDetails){
        if(userDetails == null){
            return null;
        }
        return notificationService.subscribe(userDetails.getMemberId());
    }

    //전체 알림 목록 조회
    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails){
        return ResponseEntity.ok(notificationService.getNotifications(userDetails.getMemberId()));
    }

    // 안읽은 알림 수 조회
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String,Long>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails){
        long count = notificationService.getUnreadCount(userDetails.getMemberId());
        return ResponseEntity.ok(Map.of("count",count));
    }

    //단건 읽음 처리
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        notificationService.markAsRead(notificationId,userDetails.getMemberId());
        return ResponseEntity.noContent().build();
    }

    //전체 읽음 처리
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAsAllRead(
            @AuthenticationPrincipal CustomUserDetails userDetails){
        notificationService.markAsAllRead(userDetails.getMemberId());
        return ResponseEntity.noContent().build();
    }

    //단건 삭제
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        notificationService.delete(notificationId,userDetails.getMemberId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponseDTO>> getUnreadNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails){
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userDetails.getMemberId()));
    }

}
