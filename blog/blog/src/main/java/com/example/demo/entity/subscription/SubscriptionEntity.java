package com.example.demo.entity.subscription;

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
@Table(
        name = "subscription", uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_subscription_follower_following", columnNames = {"follower_id","following_id"}
                )
}
)
@SequenceGenerator(name = "SEQ_SUBSCRIPTION_ID",sequenceName = "SEQ_SUBSCRIPTION_ID",allocationSize = 1)
public class SubscriptionEntity {

    @Id
    @GeneratedValue(generator = "SEQ_SUBSCRIPTION_ID",strategy = GenerationType.SEQUENCE)
    @Column(name = "subscription_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id",nullable = false)
    private MemberEntity following;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id",nullable = false)
    private MemberEntity follower;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;


    @Builder
    public SubscriptionEntity(MemberEntity follower,MemberEntity following){
        this.follower = follower;
        this.following= following;
    }

}
