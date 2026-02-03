package com.example.demo.repository.board;

import com.example.demo.entity.board.HashtagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HashtagRepository extends JpaRepository<HashtagEntity,Long> {

    Optional<HashtagEntity> findByName(String name);
}
