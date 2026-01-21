package com.example.demo.repository.subscription;

import com.example.demo.entity.member.MemberEntity;
import com.example.demo.entity.subscription.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity,Long> {


    Optional<SubscriptionEntity> findByFollowerAndFollowing(MemberEntity follower, MemberEntity following);

    @Query("SELECT COUNT(*) FROM SubscriptionEntity s WHERE s.follower = :follower AND s.following = :following")
    long countByFollowerAndFollowing(@Param("follower") MemberEntity follower, @Param("following") MemberEntity following);
}
