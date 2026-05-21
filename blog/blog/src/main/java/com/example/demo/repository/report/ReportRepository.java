package com.example.demo.repository.report;

import com.example.demo.entity.report.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<ReportEntity , Long> {

    //특정 회원의 신고 내역 전체 조회
    List<ReportEntity> findAllByTargetId(Long targetId);

    // 특정 회원이 신고당한 횟수
    long countByTargetId(Long targetId);
}
