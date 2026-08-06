package com.yd.todo.todo.model.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.todo.dailyList.model.dto.DailyListWithTodosResponse;
import com.yd.todo.dailyList.model.dto.DailySummaryResponse;
import com.yd.todo.dailyList.model.entity.DailyList;
import com.yd.todo.dailyList.model.repository.DailyListRepository;
import com.yd.todo.dailyList.model.service.DailyListService;
import com.yd.todo.global.exception.todo.TodoAccessDeniedException;
import com.yd.todo.global.exception.todo.TodoNotFoundException;
import com.yd.todo.todo.model.dto.TodoCreateRequest;
import com.yd.todo.todo.model.dto.TodoResponse;
import com.yd.todo.todo.model.dto.TodoStatsResponse;
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

    // 지정한 날짜(미지정 시 오늘) 리스트에 새 TODO 생성 (원본 TODO)
    @Transactional
    public TodoResponse create(Long userId, TodoCreateRequest request) {
        LocalDate targetDate = request.getTargetDate() != null ? request.getTargetDate() : LocalDate.now();
        DailyList targetList = dailyListService.getOrCreate(userId, targetDate);

        Todo todo = todoRepository.save(Todo.builder()
                .dailyList(targetList)
                .title(request.getTitle().strip())
                .memo(request.getMemo() != null ? request.getMemo().strip() : null)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .originTodoId(null)
                .build());

        todo.assignOriginIfRoot();   // 원본이므로 자기 자신의 id를 origin으로 고정

        return TodoResponse.from(todo, 1, todo.isDone());   // 방금 생성된 원본이므로 이월 일수 1
    }

    public List<TodoResponse> findTodosByDailyListId(Long dailyListId) {
        return todoRepository.findByDailyListId(dailyListId).stream()
                .map(todo -> TodoResponse.from(todo, computeCarryOverDays(todo), resolveEffectiveDone(todo)))
                .toList();
    }

    // 오늘 리스트의 TODO 전체 조회 (내일로 미룬 항목은 '오늘의 할 일'에서 제외)
    public List<TodoResponse> findTodayTodos(Long userId) {
        return findTodayDailyList(userId).getTodos();
    }

    // TODO 수정
    @Transactional
    public TodoResponse update(Long userId, Long todoId, TodoUpdateRequest request) {
        Todo todo = getOwnedTodo(userId, todoId);
        String strippedMemo = request.getMemo() != null ? request.getMemo().strip() : null;
        todo.update(request.getTitle().strip(), strippedMemo, request.getStartTime(), request.getEndTime());
        return TodoResponse.from(todo, computeCarryOverDays(todo), resolveEffectiveDone(todo));
    }

    // 완료 상태 토글
    @Transactional
    public TodoResponse toggleDone(Long userId, Long todoId) {
        Todo todo = getOwnedTodo(userId, todoId);
        todo.toggleDone();
        return TodoResponse.from(todo, computeCarryOverDays(todo), resolveEffectiveDone(todo));
    }

    // 수동 이월: 과거의 미완료 TODO를 오늘 리스트로 복제 (원본은 '밀린 할 일'에서 숨겨짐)
    @Transactional
    public TodoResponse carryOver(Long userId, Long todoId) {
        Todo origin = getOwnedTodo(userId, todoId);
        if (origin.isCarried()) {
            throw new IllegalArgumentException("이미 오늘로 가져온 할 일입니다.");
        }

        Todo copied = copyToDate(userId, origin, LocalDate.now());
        origin.markCarried(copied.getId());
        return TodoResponse.from(copied, computeCarryOverDays(copied), resolveEffectiveDone(copied));
    }

    // 오늘의 미완료 TODO를 내일 리스트로 미루기 (원본은 '오늘의 할 일'에서 숨겨짐)
    @Transactional
    public TodoResponse postponeToTomorrow(Long userId, Long todoId) {
        Todo origin = getOwnedTodo(userId, todoId);
        if (origin.isPostponed()) {
            throw new IllegalArgumentException("이미 내일로 미룬 할 일입니다.");
        }

        Todo copied = copyToDate(userId, origin, LocalDate.now().plusDays(1));
        origin.postpone(copied.getId());
        return TodoResponse.from(copied, computeCarryOverDays(copied), resolveEffectiveDone(copied));
    }

    // 내일로 미루기 취소: 복제본을 삭제하고 원본을 다시 '오늘의 할 일'에 노출
    @Transactional
    public void cancelPostpone(Long userId, Long todoId) {
        Todo origin = getOwnedTodo(userId, todoId);
        if (!origin.isPostponed()) {
            throw new IllegalArgumentException("내일로 미뤄둔 할 일이 아닙니다.");
        }

        Long postponedTodoId = origin.getPostponedTodoId();
        origin.cancelPostpone();
        todoRepository.deleteById(postponedTodoId);
    }

    // 특정 TODO를 지정한 날짜의 리스트로 복제 (이월/미루기 공통 처리)
    private Todo copyToDate(Long userId, Todo origin, LocalDate targetDate) {
        DailyList targetList = dailyListService.getOrCreate(userId, targetDate);
        Long rootOriginId = (origin.getOriginTodoId() != null) ? origin.getOriginTodoId() : origin.getId();

        return todoRepository.save(Todo.builder()
                .dailyList(targetList)
                .title(origin.getTitle())
                .memo(origin.getMemo())
                .startTime(origin.getStartTime())
                .endTime(origin.getEndTime())
                .originTodoId(rootOriginId)   // 최초 원본 id 그대로 계승
                .build());
    }

    // "며칠째"인지 계산: 같은 계보(origin) 중 이 TODO 날짜까지의 개수 (미래의 미루기 복제본은 제외)
    private int computeCarryOverDays(Todo todo) {
        Long rootOriginId = (todo.getOriginTodoId() != null) ? todo.getOriginTodoId() : todo.getId();
        return todoRepository.countByOriginTodoIdAndDailyList_ListDateLessThanEqual(
                rootOriginId, todo.getDailyList().getListDate());
    }

    // 이 TODO 자신 또는 미루기/당겨오기로 이어진 복제본 체인을 끝까지 따라가서 최종 완료 여부를 계산
    // (미룬/당겨온 원본은 자기 자신이 완료 처리될 일이 없어서 done이 항상 false로 고정되는 문제를 보완)
    private boolean resolveEffectiveDone(Todo todo) {
        Todo current = todo;
        int guard = 0;
        while (!current.isDone() && (current.isPostponed() || current.isCarried()) && guard++ < 50) {
            Long nextId = current.isPostponed() ? current.getPostponedTodoId() : current.getCarriedTodoId();
            current = todoRepository.findById(nextId).orElse(null);
            if (current == null) {
                return false;
            }
        }
        return current.isDone();
    }

    // 오늘 리스트 + 오늘의 TODO 전체를 함께 반환 (내일로 미룬 항목은 제외)
    public DailyListWithTodosResponse findTodayDailyList(Long userId) {
        DailyList todayList = dailyListService.getOrCreate(userId, LocalDate.now());
        List<TodoResponse> todos = findTodosByDailyListId(todayList.getId()).stream()
                .filter(todo -> todo.getPostponedTodoId() == null)
                .toList();
        return DailyListWithTodosResponse.of(todayList, todos);
    }

    // 사이드바 통계: 총 완료 개수 + 오늘 기준 이월 중인 개수
    public TodoStatsResponse getStats(Long userId) {
        long completedCount = todoRepository.countByDailyList_User_IdAndDoneTrue(userId);

        DailyListWithTodosResponse today = findTodayDailyList(userId);
        long carryingOverCount = today.getTodos().stream()
                .filter(todo -> !todo.isDone() && todo.getCarryOverDays() > 1)
                .count();

        return TodoStatsResponse.builder()
                .completedCount(completedCount)
                .carryingOverCount(carryingOverCount)
                .build();
    }

    // 밀린 할 일: 오늘 내일로 미뤄둔 TODO + 오늘 이전 날짜의 미완료 TODO를 날짜별로 모아서 반환 (최근 날짜부터)
    public List<DailyListWithTodosResponse> getIncompleteHistory(Long userId) {
        List<Todo> postponedToday = todoRepository.findByDailyList_User_IdAndDoneFalseAndPostponedTodoIdIsNotNull(userId);

        List<Todo> pastIncomplete = todoRepository
                .findByDailyList_User_IdAndDoneFalseAndDailyList_ListDateLessThanOrderByDailyList_ListDateDesc(
                        userId, LocalDate.now())
                .stream()
                .filter(todo -> !todo.isCarried())   // 이미 오늘로 당겨온 항목은 '밀린 할 일'에서 제외
                .toList();

        List<Todo> all = new ArrayList<>(postponedToday);
        all.addAll(pastIncomplete);

        Map<LocalDate, List<Todo>> groupedByDate = all.stream()
                .collect(Collectors.groupingBy(
                        todo -> todo.getDailyList().getListDate(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        return groupedByDate.values().stream()
                .map(todos -> DailyListWithTodosResponse.of(
                        todos.get(0).getDailyList(),
                        todos.stream()
                                .map(todo -> TodoResponse.from(todo, computeCarryOverDays(todo), resolveEffectiveDone(todo)))
                                .toList()))
                .toList();
    }

    // 달력 마킹용: 기간 내 날짜별 TODO 총 개수/완료 개수 요약
    public List<DailySummaryResponse> getSummary(Long userId, LocalDate start, LocalDate end) {
        List<Todo> todos = todoRepository.findByDailyList_User_IdAndDailyList_ListDateBetween(userId, start, end);

        Map<LocalDate, List<Todo>> groupedByDate = todos.stream()
                .collect(Collectors.groupingBy(todo -> todo.getDailyList().getListDate()));

        return groupedByDate.entrySet().stream()
                .map(entry -> DailySummaryResponse.builder()
                        .date(entry.getKey())
                        .total(entry.getValue().size())
                        .done((int) entry.getValue().stream().filter(Todo::isDone).count())
                        .build())
                .toList();
    }

    // TODO 삭제 (이 TODO가 누군가의 '내일로 미루기' 복제본이었다면, 그 원본의 미루기 상태도 함께 정리)
    @Transactional
    public void delete(Long userId, Long todoId) {
        Todo todo = getOwnedTodo(userId, todoId);
        todoRepository.findByPostponedTodoId(todoId).ifPresent(Todo::cancelPostpone);
        todoRepository.findByCarriedTodoId(todoId).ifPresent(Todo::cancelCarry);
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