package com.example.demo.entity.member;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SequenceGenerator(
        name = "member",
        sequenceName = "member_seq",
        allocationSize = 1
)
@Table(name = "member")
@Builder
public class MemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member")
    @Column
    private Long memberId;

    @Column
    private String username;

    @Column
    private String password;

    @Column
    private String nickname;

    @Column
    private String email;

    @Column
    private String role;

    @Column
    private String provider;

    @Column
    private String providerId;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status ="ACTIVE";

    public void block(){
        this.status="BLOCKED";
    }

    public void unblock(){
        this.status="ACTIVE";
    }

    public boolean isBlocked(){
        return "BLOCKED".equals(this.status);
    }



}
