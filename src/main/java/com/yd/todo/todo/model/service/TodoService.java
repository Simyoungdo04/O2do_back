package com.yd.todo.todo.model.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.todo.dailyList.model.dto.DailyListWithTodosResponse;
import com.yd.todo.dailyList.model.entity.DailyList;
import com.yd.todo.dailyList.model.repository.DailyListRepository;
import com.yd.todo.dailyList.model.service.DailyListService;
import com.yd.todo.global.exception.todo.TodoAccessDeniedException;
import com.yd.todo.global.exception.todo.TodoNotFoundException;
import com.yd.todo.todo.model.dto.TodoCreateRequest;
import com.yd.todo.todo.model.dto.TodoResponse;
import com.yd.todo.todo.model.dto.TodoUpdateRequest;
import com.yd.todo.todo.model.entity.Todo;
import com.yd.todo.todo.model.repository.TodoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final DailyListService dailyListService;
    private final DailyListRepository dailyListRepository;

    // 오늘 리스트에 새 TODO 생성 (원본 TODO)
    @Transactional
    public TodoResponse create(Long userId, TodoCreateRequest request) {
        DailyList todayList = dailyListService.getOrCreate(userId, LocalDate.now());

        Todo todo = todoRepository.save(Todo.builder()
                .dailyList(todayList)
                .title(request.getTitle())
                .memo(request.getMemo())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .originTodoId(null)
                .build());

        todo.assignOriginIfRoot();   // 원본이므로 자기 자신의 id를 origin으로 고정

        return TodoResponse.from(todo, 1);   // 방금 생성된 원본이므로 이월 일수 1
    }

    public List<TodoResponse> findTodosByDailyListId(Long dailyListId) {
        return todoRepository.findByDailyListId(dailyListId).stream()
                .map(todo -> TodoResponse.from(todo, todoRepository.countByOriginTodoId(todo.getOriginTodoId())))
                .toList();
    }
    
    // 오늘 리스트의 TODO 전체 조회
    public List<TodoResponse> findTodayTodos(Long userId) {
        DailyList todayList = dailyListService.getOrCreate(userId, LocalDate.now());
        return findTodosByDailyListId(todayList.getId());
    }

    // TODO 수정
    @Transactional
    public TodoResponse update(Long userId, Long todoId, TodoUpdateRequest request) {
        Todo todo = getOwnedTodo(userId, todoId);
        todo.update(request.getTitle(), request.getMemo(), request.getStartTime(), request.getEndTime());
        return TodoResponse.from(todo, todoRepository.countByOriginTodoId(todo.getOriginTodoId()));
    }

    // 완료 상태 토글
    @Transactional
    public TodoResponse toggleDone(Long userId, Long todoId) {
        Todo todo = getOwnedTodo(userId, todoId);
        todo.toggleDone();
        return TodoResponse.from(todo, todoRepository.countByOriginTodoId(todo.getOriginTodoId()));
    }

    // 수동 이월: 어제(과거)의 미완료 TODO를 오늘 리스트로 복제
    @Transactional
    public TodoResponse carryOver(Long userId, Long todoId) {
        Todo origin = getOwnedTodo(userId, todoId);
        DailyList todayList = dailyListService.getOrCreate(userId, LocalDate.now());

        Long rootOriginId = (origin.getOriginTodoId() != null) ? origin.getOriginTodoId() : origin.getId();

        Todo carried = todoRepository.save(Todo.builder()
                .dailyList(todayList)
                .title(origin.getTitle())
                .memo(origin.getMemo())
                .startTime(origin.getStartTime())
                .endTime(origin.getEndTime())
                .originTodoId(rootOriginId)   // 최초 원본 id 그대로 계승
                .build());

        return TodoResponse.from(carried, todoRepository.countByOriginTodoId(rootOriginId));
    }
    
    // 오늘 리스트 + 오늘의 TODO 전체를 함께 반환
    public DailyListWithTodosResponse findTodayDailyList(Long userId) {
        DailyList todayList = dailyListService.getOrCreate(userId, LocalDate.now());
        List<TodoResponse> todos = findTodosByDailyListId(todayList.getId());   // findTodayTodos 대신 공통 메서드 직접 호출
        return DailyListWithTodosResponse.of(todayList, todos);
    }

    // TODO 삭제
    @Transactional
    public void delete(Long userId, Long todoId) {
        Todo todo = getOwnedTodo(userId, todoId);
        todoRepository.delete(todo);
    }

    // 조회 + 소유권 검증 공통 처리
    private Todo getOwnedTodo(Long userId, Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new TodoNotFoundException("존재하지 않는 할 일입니다."));

        if (!todo.isOwnedBy(userId)) {
            throw new TodoAccessDeniedException("본인의 할 일만 접근할 수 있습니다.");
        }

        return todo;
    }

}