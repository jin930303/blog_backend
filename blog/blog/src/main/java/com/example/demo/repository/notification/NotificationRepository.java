package com.example.demo.repository.notification;

import com.example.demo.entity.member.MemberEntity;
import com.example.demo.entity.notification.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<NotificationEntity,Long> {

    List<NotificationEntity> findAllByReceiverOrderByCreatedAtDesc(MemberEntity receiver);

    long countByReceiverAndIsReadFalse(MemberEntity receiver);

    Optional<NotificationEntity> findByIdAndReceiver(Long id, MemberEntity receiver);


    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true WHERE n.receiver = :receiver AND n.isRead = false")
    void markAsAllRead(@Param("receiver") MemberEntity receiver);

    List<NotificationEntity> findAllByReceiverAndIsReadFalseOrderByCreatedAtDesc(MemberEntity receiver);
}
