package com.example.demo.entity.notification;

import com.example.demo.entity.member.MemberEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification")
@SequenceGenerator(name = "seq_notification_id",allocationSize = 1,initialValue = 1,sequenceName = "seq_notification_id")
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "seq_notification_id")
    @Column(name = "notification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id",nullable = false)
    private MemberEntity receiver;

    @Column(nullable = false)
    private String content;

    private String url;

    @Column(nullable = false)
    private boolean isRead;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public NotificationEntity(MemberEntity receiver, String content, String url, boolean isRead){
        this.receiver = receiver;
        this.content = content;
        this.url = url;
        this.isRead = isRead;
    }

    public void read(){
        this.isRead = true;
    }



}
