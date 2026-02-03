package com.example.demo.service.notification;

import com.example.demo.entity.member.MemberEntity;
import com.example.demo.entity.notification.NotificationEntity;
import com.example.demo.repository.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService{

    private final NotificationRepository notificationRepository;

    private static final Map<Long,SseEmitter> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter subscribe(Long memberId) {

        SseEmitter emitter = new SseEmitter(60L * 1000 * 60);
        emitters.put(memberId,emitter);

        emitter.onCompletion(()->emitters.remove(memberId));
        emitter.onTimeout(()->emitters.remove(memberId));

        try{
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected!"));
        } catch (IOException e){
            emitters.remove(memberId);
        }

        return emitter;
    }

    @Transactional
    public void send(MemberEntity receiver, String content, String url){
        NotificationEntity notification = NotificationEntity.builder()
                .receiver(receiver)
                .content(content)
                .url(url)
                .isRead(false)
                .build();
        notificationRepository.save(notification);

        // 해당 유저가 접속해있을경우 실시간 알림 전송
        Long receiverId = receiver.getMemberId();
        SseEmitter emitter = emitters.get(receiverId);

        if(emitter !=null){
            try{
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(content));
            } catch (IOException e){
                emitters.remove(receiverId);
                log.error("알림 전송 실패 : {}",e.getMessage());
            }
        }
    }
}
