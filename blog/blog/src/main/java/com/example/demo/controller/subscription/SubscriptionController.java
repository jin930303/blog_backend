package com.example.demo.controller.subscription;

import com.example.demo.service.member.CustomUserDetails;
import com.example.demo.service.subscription.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscriptions")
@Slf4j
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/{targetId}")
    public ResponseEntity<Boolean> toggleSubscription(@PathVariable Long targetId,
                                                      @AuthenticationPrincipal CustomUserDetails userDetails){
//        if(userDetails ==null){
//            log.warn("인증 정보 없음");
//            return ResponseEntity.status(401).build();
//        }

//        Long currentMemberId = userDetails.getMemberId();

//        boolean result = subscriptionService.toggleSubscription(userDetails.getMemberId(),targetId);

        return ResponseEntity.ok(subscriptionService.toggleSubscription(userDetails.getMemberId(),targetId));
    }

    @GetMapping("/{targetId}")
    public ResponseEntity<Boolean> checkSubscriptionStatus(
            @PathVariable Long targetId,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        Long currentMemberId = (userDetails !=null) ? userDetails.getMemberId() : null;

//        boolean result = subscriptionService.checkSubscriptionStatus(currentMemberId,targetId);

        return ResponseEntity.ok(subscriptionService.checkSubscriptionStatus(currentMemberId,targetId));
    }

}
