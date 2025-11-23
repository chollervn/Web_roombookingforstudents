package com.ecom.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "game_records")
public class GameRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserDtls user;

    @Column(nullable = false, length = 50, name = "game_type")
    private String gameType = "DUCK_RACE";

    @Column(nullable = false, length = 10)
    private String result; // WIN or LOSE

    @Column(nullable = false, name = "selected_duck")
    private Integer selectedDuck; // 1-6

    @Column(nullable = false, name = "winning_duck")
    private Integer winningDuck; // 1-6

    @ManyToOne
    @JoinColumn(name = "voucher_id")
    private Voucher voucher; // Nullable, only set if won

    @Column(name = "played_date")
    private LocalDateTime playedDate;
}
