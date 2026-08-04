package com.yd.todo.dailyList.model.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.todo.dailyList.model.entity.DailyList;
import com.yd.todo.dailyList.model.repository.DailyListRepository;
import com.yd.todo.global.exception.dailylist.DailyListNotFoundException;
import com.yd.todo.user.model.entity.User;
import com.yd.todo.user.model.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DailyListService {

    private final DailyListRepository dailyListRepository;
    private final UserRepository userRepository;

    // 특정 날짜의 리스트 조회, 없으면 새로 생성
    @Transactional
    public DailyList getOrCreate(Long userId, LocalDate date) {
        return dailyListRepository.findByUserIdAndListDate(userId, date)
                .orElseGet(() -> create(userId, date));
    }
    
    public DailyList getByDate(Long userId, LocalDate date) {
        return dailyListRepository.findByUserIdAndListDate(userId, date)
                .orElseThrow(() -> new DailyListNotFoundException("해당 날짜의 기록이 없습니다."));
    }

    private DailyList create(Long userId, LocalDate date) {
        User user = userRepository.getReferenceById(userId);   // 실제 SELECT 없이 프록시 참조만 획득
        return dailyListRepository.save(
                DailyList.builder()
                        .user(user)
                        .listDate(date)
                        .build()
        );
    }
}