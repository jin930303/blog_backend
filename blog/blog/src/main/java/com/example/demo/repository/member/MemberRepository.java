package com.example.demo.repository.member;

import com.example.demo.entity.member.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    Optional<MemberEntity> findByUsername(String username);

    @Query(value = "SELECT COUNT(username) FROM member WHERE username = :username", nativeQuery = true)
    int countByUsername(@Param("username") String username);

    // 💡 닉네임 중복 확인을 위한 Native Query (추가 예정)
    @Query(value = "SELECT COUNT(nickname) FROM member WHERE nickname = :nickname", nativeQuery = true)
    int countByNickname(@Param("nickname") String nickname);

    @Query("SELECT m.nickname FROM MemberEntity m WHERE m.username = :username")
    Optional<String> findNicknameByUsername(@Param("username") String username);

    // 카카오
    Optional<MemberEntity> findByProviderId(String id);

    Optional<MemberEntity> findByProviderAndProviderId(String provider, String providerId);

    @Query("SELECT m.username FROM MemberEntity m WHERE m.email = :email")
    Optional<String> findUsernameByEmail(@Param("email") String email);

    Optional<MemberEntity> findByUsernameAndEmail(String username, String email);
}
