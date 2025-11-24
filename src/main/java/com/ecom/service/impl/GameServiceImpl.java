package com.ecom.service.impl;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.model.GameRecord;
import com.ecom.model.UserDtls;
import com.ecom.model.Voucher;
import com.ecom.repository.GameRecordRepository;
import com.ecom.repository.UserRepository;
import com.ecom.service.GameService;
import com.ecom.service.VoucherService;

@Service
public class GameServiceImpl implements GameService {

    @Autowired
    private GameRecordRepository gameRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VoucherService voucherService;

    private Random random = new Random();

    @Override
    public GameRecord recordGamePlay(Long userId, Integer selectedDuck, Integer winningDuck) {
        UserDtls user = userRepository.findById(userId.intValue()).orElse(null);
        if (user == null) {
            return null;
        }

        GameRecord record = new GameRecord();
        record.setUser(user);
        record.setGameType("DUCK_RACE");
        record.setSelectedDuck(selectedDuck);
        record.setWinningDuck(winningDuck);
        record.setPlayedDate(LocalDateTime.now());

        // Check if player won
        boolean won = selectedDuck.equals(winningDuck);
        record.setResult(won ? "WIN" : "LOSE");

        // If won, create voucher with random discount 5-20%
        if (won) {
            int discount = 5 + random.nextInt(16); // 5 to 20
            Voucher voucher = voucherService.createVoucher(userId.intValue(), discount);
            record.setVoucher(voucher);
        }

        return gameRecordRepository.save(record);
    }

    @Override
    public List<GameRecord> getUserGameHistory(Long userId) {
        return gameRecordRepository.findByUserIdOrderByPlayedDateDesc(userId);
    }

    @Override
    public Long getTodayPlayCount(Long userId) {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        return gameRecordRepository.countByUserIdAndPlayedDateAfter(userId, startOfDay);
    }
}
