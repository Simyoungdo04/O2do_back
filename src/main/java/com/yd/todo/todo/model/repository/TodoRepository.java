package com.yd.todo.todo.model.repository;

import com.yd.todo.todo.model.entity.Todo;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    // 특정 리스트(하루)에 속한 TODO 전체 조회
    List<Todo> findByDailyListId(Long dailyListId);

    // 특정 TODO를 내일로 미룬 원본 찾기 (미룬 복제본을 직접 지울 때 원본의 미루기 상태도 함께 정리하기 위함)
    Optional<Todo> findByPostponedTodoId(Long postponedTodoId);

    // 특정 TODO를 오늘로 당겨온 원본 찾기 (당겨온 복제본을 직접 지울 때 원본의 당겨오기 상태도 함께 정리하기 위함)
    Optional<Todo> findByCarriedTodoId(Long carriedTodoId);

    // 오늘 이전 날짜 중 아직 완료하지 못한 TODO 전체 (날짜별로 모아서 보여주기용), 최근 날짜부터
    List<Todo> findByDailyList_User_IdAndDoneFalseAndDailyList_ListDateLessThanOrderByDailyList_ListDateDesc(
            Long userId, LocalDate date);

    // 내일로 미뤄둔(아직 완료 전) TODO 전체 ('밀린 할 일'에 함께 모아 보여주기용)
    List<Todo> findByDailyList_User_IdAndDoneFalseAndPostponedTodoIdIsNotNull(Long userId);

    // 특정 리스트에서 미완료 TODO만 조회 (이월 대상 선별용)
    List<Todo> findByDailyListIdAndDoneFalse(Long dailyListId);

    // 특정 기간(달력 조회용) 내 TODO 전체
    List<Todo> findByDailyList_User_IdAndDailyList_ListDateBetween(Long userId, LocalDate start, LocalDate end);

    // 같은 origin_todo_id를 가진 TODO 중 특정 날짜까지의 개수 (그 날짜 기준 "며칠째"인지 계산용, 미래의 미루기 복제본은 제외)
    int countByOriginTodoIdAndDailyList_ListDateLessThanEqual(Long originTodoId, LocalDate date);

    // 유저가 지금까지 완료한 TODO 총 개수 (통계용)
    long countByDailyList_User_IdAndDoneTrue(Long userId);

    // 회원 탈퇴 시 해당 유저의 TODO 전체 삭제
    @Transactional
    void deleteByDailyList_User_Id(Long userId);
}