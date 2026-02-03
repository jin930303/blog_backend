package com.example.demo.repository.notification;

import com.example.demo.entity.member.MemberEntity;
import com.example.demo.entity.notification.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity,Long> {

    List<NotificationEntity> findAllByReceiverOrderByCreatedAtDesc(MemberEntity receiver);

    long countByReceiverAndIsReadFalse(MemberEntity receiver);
}
