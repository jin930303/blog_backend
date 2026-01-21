package com.example.demo.service.subscription;

import com.example.demo.entity.member.MemberEntity;
import com.example.demo.entity.subscription.SubscriptionEntity;
import com.example.demo.repository.member.MemberRepository;
import com.example.demo.repository.subscription.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService{

    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;

    @Transactional
    @Override
    public boolean toggleSubscription(Long currentMemberId, Long targetId) {

        MemberEntity follower = memberRepository.findById(currentMemberId)
                .orElseThrow(()-> new IllegalArgumentException("로그인 사용자가 존재하지 않습니다."));

        MemberEntity following = memberRepository.findById(targetId)
                .orElseThrow(()-> new IllegalArgumentException("대상 사용자가 존재하지 않습니다."));

        if(follower.getMemberId() == following.getMemberId()){
            throw new IllegalStateException("자기 자신은 구독할 수 없습니다.");
        }
        return subscriptionRepository.findByFollowerAndFollowing(follower,following)
                .map(subscription -> {
                    subscriptionRepository.delete(subscription);
                    return false;
                })
                .orElseGet(()->{
                    SubscriptionEntity subscription = SubscriptionEntity.builder()
                            .follower(follower)
                            .following(following)
                            .build();
                    subscriptionRepository.save(subscription);
                    return true;
                });
    }

    @Transactional(readOnly = true)
    @Override
    public boolean checkSubscriptionStatus(Long currentMemberId, Long targetId) {

        if(currentMemberId == null){
            return false;
        }

        MemberEntity follower = memberRepository.findById(currentMemberId)
                .orElseThrow(()-> new IllegalArgumentException("로그인 사용자가 존재하지 않습니다."));

        MemberEntity following = memberRepository.findById(targetId)
                .orElseThrow(()-> new IllegalArgumentException("대상 사용자가 존재하지 않습니다."));
        return subscriptionRepository.countByFollowerAndFollowing(follower,following) >0;
    }
}
