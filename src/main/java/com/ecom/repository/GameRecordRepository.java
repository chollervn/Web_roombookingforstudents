package com.ecom.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.model.GameRecord;

public interface GameRecordRepository extends JpaRepository<GameRecord, Long> {

    // Find game history for a user
    List<GameRecord> findByUserIdOrderByPlayedDateDesc(Long userId);

    // Count games played by user after a certain date
    Long countByUserIdAndPlayedDateAfter(Long userId, LocalDateTime date);
}
