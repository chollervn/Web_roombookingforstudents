package com.ecom.service;

import java.util.List;

import com.ecom.model.GameRecord;

public interface GameService {

    // Record game play and create voucher if won
    GameRecord recordGamePlay(Long userId, Integer selectedDuck, Integer winningDuck);

    // Get game history for a user
    List<GameRecord> getUserGameHistory(Long userId);

    // Get count of games played today
    Long getTodayPlayCount(Long userId);
}
