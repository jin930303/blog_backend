package com.example.demo.service.member;

import com.example.demo.dto.board.MyBoardResponseDTO;
import com.example.demo.dto.member.NicknameChangeRequestDTO;
import com.example.demo.dto.member.PwChangeRequestDTO;
import com.example.demo.dto.member.google.GoogleUserInfoDTO;
import com.example.demo.dto.member.kakao.KakaoUserInfoDTO;
import com.example.demo.dto.member.MemberDTO;
import com.example.demo.entity.member.MemberEntity;
import com.example.demo.repository.board.BoardRepository;
import com.example.demo.repository.member.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService{

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BoardRepository boardRepository;

    @Override
    public void signup_save(MemberDTO dto) {
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        MemberEntity entity = new MemberEntity();
        entity.setUsername(dto.getUsername());
        entity.setPassword(encodedPassword);
        entity.setNickname(dto.getNickname());
        entity.setEmail(dto.getEmail());
        entity.setRole("ROLE_USER");

        memberRepository.save(entity);
    }

    @Override
    public boolean checkUsername(String username) {
        return memberRepository.countByUsername(username) > 0;
    }

    @Override
    public boolean checkNickname(String nickname) {
        return memberRepository.countByNickname(nickname) > 0;
    }

    @Override
    public String getNicknameByUsername(String username) {
        return memberRepository.findNicknameByUsername(username).orElse("사용자");
    }

    // 카카오 로그인 저장
    @Override
    @Transactional // DB 변경이 발생하므로 트랜잭션 처리
    public String socialLoginOrSignupAndGetJwt(KakaoUserInfoDTO userInfo) {
        // 1. providerId(카카오 id)로 기존 회원 찾기
        String providerIdString = userInfo.getId().toString();
        Optional<MemberEntity> memberOptional = memberRepository.findByProviderId(providerIdString);

        MemberEntity entity;
        // providerId가 0개 or 1개일 때 안전하게 작동함
        if(memberOptional.isPresent()) {
            // 2. 기존 회원: 로그인 처리
            entity = memberOptional.get();
        } else {
            // 3. 신규 회원: 회원가입 처리 (DB 저장)
            entity = new MemberEntity();
            entity.setProvider("KAKAO");
            entity.setProviderId(userInfo.getId().toString());
            entity.setUsername("kakao_" + userInfo.getId());
            entity.setNickname(userInfo.getNickname());
            entity.setEmail(userInfo.getEmail());
            // pw 기본이 null 허용 x라 임시 비밀번호를 SOCIAL_providerId로
            String tempPassword = "SOCIAL_" + userInfo.getId();
            entity.setPassword(passwordEncoder.encode(tempPassword));
            entity.setRole("ROLE_USER");

            entity = memberRepository.save(entity);
        }

        // 4. JWT 토큰 발행
        return jwtTokenProvider.createToken(entity.getUsername(), entity.getRole(), entity.getNickname());
    }

    @Override
    public MemberDTO getMemberInfoById(Long memberId) {
        MemberEntity memberEntity = memberRepository.findById(memberId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 ID의 사용자 정보를 찾을 수 없습니다."));
        // Entity를 DTO로 변환하여 반환
        return new MemberDTO(
                memberEntity.getMemberId(),     // 1. long (memberId)
                memberEntity.getUsername(),     // 2. String (username)
                memberEntity.getPassword(),     // 3. String (password)
                memberEntity.getNickname(),     // 4. String (nickname)
                memberEntity.getEmail(),        // 5. String (email)
                memberEntity.getRole(),         // 6. String (role)
                memberEntity.getProvider(),     // 7. String (provider)
                memberEntity.getProviderId()    // 8. String (providerId)
        );
    }

    @Override
    public String googleLoginOrSignupAndGetJwt(GoogleUserInfoDTO userinfo) {

        MemberEntity member = memberRepository
                .findByProviderAndProviderId("google",userinfo.getId())
                .orElseGet(()->{
                    MemberEntity newMember = MemberEntity.builder()
                            .username("google_"+userinfo.getId())
                            .password(UUID.randomUUID().toString())
                            .nickname(userinfo.getNickname())
                            .email(userinfo.getEmail())
                            .role("ROLE_USER")
                            .provider("google")
                            .providerId(userinfo.getId())
                            .build();
                    return memberRepository.save(newMember);
                });

        return jwtTokenProvider.createToken(
                member.getUsername(),
                member.getRole(),
                member.getNickname()
        );
    }

    @Override
    @Transactional
    public void changePw(Long memberId, PwChangeRequestDTO dto) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));


        // 새 비밀번호 === 현재 비밀번호 체크
        if(passwordEncoder.matches(dto.getNewPw(),member.getPassword())){
            throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 달라야합니다.");
        }

        //새 비밀번호 === 새 비밀번호 확인 체크
        if(!dto.getNewPw().equals(dto.getNewPwConfirm())){
            throw new IllegalArgumentException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        member.setPassword(passwordEncoder.encode(dto.getNewPw()));
    }

    //닉네임 변경
    @Override
    @Transactional
    public void changeNickname(NicknameChangeRequestDTO dto, Long memberId) {
        MemberEntity member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        //현재 닉네임과 동일한지 체크
        if(member.getNickname().equals(dto.getNickname())){
            throw new IllegalArgumentException("현재 닉네임과 동일합니다.");
        }

        //중복 체크
        if(memberRepository.countByNickname(dto.getNickname())>0){
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
        }
        member.setNickname(dto.getNickname());

    }

    @Override
    public List<MyBoardResponseDTO> getMyBoards(Long memberId) {
        MemberEntity member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        return boardRepository.findAllByMemberOrderByInputDateDesc(member)
                .stream()
                .map(MyBoardResponseDTO :: from)
                .toList();
    }

    @Override
    public void verifyCurrentPassword(Long memberId, String currentPw) {
        MemberEntity member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        if(!passwordEncoder.matches(currentPw,member.getPassword())){
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
    }
}
