package com.example.demo.service.notification;

import com.example.demo.dto.notification.NotificationResponseDTO;
import com.example.demo.entity.member.MemberEntity;
import com.example.demo.entity.notification.NotificationEntity;
import com.example.demo.repository.member.MemberRepository;
import com.example.demo.repository.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService{

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

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
                        .data(Map.of("content",content,"url",url)));
            } catch (IOException e){
                emitters.remove(receiverId);
                log.error("알림 전송 실패 : {}",e.getMessage());
            }
        }
    }

    //전체 알림 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getNotifications(Long memberId) {
        MemberEntity member = getMember(memberId);
        return notificationRepository.findAllByReceiverOrderByCreatedAtDesc(member)
                .stream()
                .map(NotificationResponseDTO :: from)
                .toList();
    }

    // 안읽은 알림 수 조회
    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long memberId) {
        MemberEntity member = getMember(memberId);
        return notificationRepository.countByReceiverAndIsReadFalse(member);
    }

    //단건 읽음 처리
    @Override
    @Transactional
    public void markAsRead(Long notificationId,Long memberId) {
        MemberEntity member = getMember(memberId);
        NotificationEntity notification = notificationRepository
                .findByIdAndReceiver(notificationId,member)
                .orElseThrow(() ->new IllegalArgumentException("알림을 찾을 수 없습니다. id="+notificationId));
        notification.read();
    }

    //전체 읽음 처리
    @Override
    @Transactional
    public void markAsAllRead(Long memberId) {
        MemberEntity member = getMember(memberId);
        notificationRepository.markAsAllRead(member);

    }

    // 단건 삭제
    @Override
    @Transactional
    public void delete(Long notificationId, Long memberId) {
        MemberEntity member = getMember(memberId);
        NotificationEntity notification = notificationRepository
                .findByIdAndReceiver(notificationId,member)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다. id="+notificationId));
        notificationRepository.delete(notification);

    }

    @Override
    public List<NotificationResponseDTO> getUnreadNotifications(Long memberId) {
        MemberEntity member = getMember(memberId);
        return notificationRepository
                .findAllByReceiverAndIsReadFalseOrderByCreatedAtDesc(member)
                .stream()
                .map(NotificationResponseDTO :: from)
                .toList();
    }

    // 유틸
    private MemberEntity getMember(Long memberId){
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. id="+memberId));
    }
}
