package com.example.demo.service.email;

import com.example.demo.entity.member.MemberEntity;
import com.example.demo.repository.member.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService{

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // Redis 키 prefix
    private static final String VERIFY_CODE_PREFIX = "email:verify:"; // 인증 코드 (TTL 5분)
    private static final String VERIFIED_PREFIX = "email:verified:"; // 인증 완료 (TTL 10분)
    private static final String RESET_TOKEN_PREFIX = "reset:"; //비밀번호 재설정 토큰 (TTL 30분)

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    // 인증 코드 발송
    @Override
    public void sendVerificationCode(String email) {
        String code = generateCode();

        // Redis 저장 (5분)
        redisTemplate.opsForValue().set(
                VERIFY_CODE_PREFIX + email,
                code,
                Duration.ofMinutes(5)
        );

        // 메일 발송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[Semicolon] 이메일 인증 코드");
        message.setText(
                "안녕하세요 Semicolon입니다 \n\n"+
                        "인증 코드 : "+code + "\n\n"+
                        "인증 코드는 5분간 유효합니다. \n" +
                        "본인이 요청하지 않은 경우 이 메일을 무시해주세요."
        );
        mailSender.send(message);
        log.info("[Email] 인증 코드 발송 완료 - email={}",email);

    }

    // 인증 코드 검증
    @Override
    public boolean verifyCode(String email, String code) {
        String saveCode = redisTemplate.opsForValue().get(VERIFY_CODE_PREFIX+email);

        if(saveCode == null || !saveCode.equals(code)){
            log.warn("[Email] 인증 코드 불일치 : email={}",email);
            return  false;
        }

        // 인증 완료 표시 저장 (10분)
        redisTemplate.opsForValue().set(
                VERIFIED_PREFIX + email,
                "true",
                Duration.ofMinutes(10)
        );

        // 사용한 인증 코드 삭제
        redisTemplate.delete(VERIFY_CODE_PREFIX+email);
        log.info("[Email] 인증 완료 -email={}",email);
        return true;
    }

    // 아이디 찾기
    @Override
    public void sendUsername(String email) {
        String username = memberRepository.findUsernameByEmail(email)
                .orElseThrow(()-> new IllegalArgumentException("해당 이메일로 가입된 계정이 없습니다."));

        String maskedUsername = maskUsername(username);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[Semicolon] 아이디 찾기 결과");
        message.setText(
                "안녕하세요 Semicolon 입니다. \n\n"+
                        "요청하신 계정의 아이디입니다. \n\n" +
                        "아이디 :" + maskedUsername +"\n\n" +
                "보안을 위해 일부 문자는 * 로 표시됩니다."
        );
        mailSender.send(message);
        log.info("[Email] 아이디 찾기 발송 완료 -email = {}",email);
    }

    // 유틸
    /* 6자리 랜덤 숫자 코드 발송*/
    private String generateCode(){
        return String.format("%06d",new Random().nextInt(1_000_000));
    }

    /**
     * 아이디 마스킹
     * 앞 2자리만 노출 나미저 * 처리
     */
    private String maskUsername(String username){
        if(username.length() <= 2) return username;
        return username.substring(0,2) + "*".repeat(username.length()-2);
    }

    /** 이메일 인증 완료 여부 확인 (회원가입 시 검증용)*/
    public boolean isVerified(String email){
        return Boolean.parseBoolean(redisTemplate.opsForValue().get(VERIFIED_PREFIX+email));
    }

    /** 인증 완료 키 삭제 (회원가입 완료 후 처리)*/
    public void deleteVerified(String email){
        redisTemplate.delete(VERIFIED_PREFIX+email);
    }


    // 비밀번호 재설정 링크 발송
    @Override
    public void sendPasswordResetLink(String username, String email) {
        MemberEntity member = memberRepository.findByUsernameAndEmail(username,email)
                .orElseThrow(()->new IllegalArgumentException("아이디와 이메일 정보가 일치하지 않습니다."));

        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                RESET_TOKEN_PREFIX + token,
                String.valueOf(member.getMemberId()),
                Duration.ofMinutes(30)
        );

        String resetLink = frontendUrl + "/reset-password?token="+token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[Semicolon] 비밀번호 재설정 링크");
        message.setText(
                "안녕하세요 Semicolon입니다. \n\n"+
                "아래 링크를 클릭하여 새 비밀번호를 설정해주세요\n"+
                        resetLink+"\n\n"+
                        "링크는 30분간 유효합니다.\n"+
                        "본인이 요청하지 않은 경우 이 메일을 무시해주세요."
        );
        mailSender.send(message);
        log.info("[Email] 비밀번호 재설정 링크 발송 완료 -email = {}",email);
    }

    // 토큰 유효성 확인 (프론트 진입 시 사전 체크용)
    @Override
    public boolean isValidResetToken(String token) {
        return redisTemplate.hasKey(RESET_TOKEN_PREFIX + token);
    }

    // 비밀번호 재설정 관리
    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        String memberIdStr = redisTemplate.opsForValue().get(RESET_TOKEN_PREFIX + token);
        if(memberIdStr == null){
            throw new IllegalArgumentException("유효하지 않거나 만료된 링크입니다.");
        }
        Long memberId = Long.parseLong(memberIdStr);
        MemberEntity member = memberRepository.findById(memberId).orElseThrow(()-> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        if(passwordEncoder.matches(newPassword,member.getPassword())){
            throw new IllegalArgumentException("현재 비밀번호와 동일한 비밀번호로 변경할 수 없습니다.");
        }

        member.setPassword(passwordEncoder.encode(newPassword));

        redisTemplate.delete(RESET_TOKEN_PREFIX+token);
        log.info("[Email] 비밀번호 재설정 완료 memberId={}",memberId);
    }
}

