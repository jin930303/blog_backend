package com.example.demo.entity.member;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@SequenceGenerator(
        name = "member",
        sequenceName = "member_seq",
        allocationSize = 1
)
@Table(name = "member")
public class MemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member")
    @Column
    private long memberId;

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


    public MemberEntity() {}
}
