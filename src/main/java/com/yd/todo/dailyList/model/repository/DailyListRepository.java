package com.yd.todo.dailyList.model.repository;

import com.yd.todo.dailyList.model.entity.DailyList;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface DailyListRepository extends JpaRepository<DailyList, Long> {

    // 특정 유저의 특정 날짜 리스트 조회 (오늘 리스트 존재 여부 확인용)
    Optional<DailyList> findByUserIdAndListDate(Long userId, LocalDate listDate);

    // 특정 날짜보다 이전 리스트 중 가장 최근 것 (이월 소스 찾기용)
    Optional<DailyList> findTop1ByUserIdAndListDateLessThanOrderByListDateDesc(Long userId, LocalDate listDate);

    // 회원 탈퇴 시 해당 유저의 DAILY_LIST 전체 삭제
    @Transactional
    void deleteByUserId(Long userId);
}