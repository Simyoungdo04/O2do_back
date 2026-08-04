package com.yd.todo.todo.model.repository;

import com.yd.todo.todo.model.entity.Todo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    // 특정 리스트(하루)에 속한 TODO 전체 조회
    List<Todo> findByDailyListId(Long dailyListId);

    // 특정 리스트에서 미완료 TODO만 조회 (이월 대상 선별용)
    List<Todo> findByDailyListIdAndDoneFalse(Long dailyListId);

    // 같은 origin_todo_id를 가진 TODO 개수 (이월 일수 계산용)
    int countByOriginTodoId(Long originTodoId);
}