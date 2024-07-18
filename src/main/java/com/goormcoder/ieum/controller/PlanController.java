package com.goormcoder.ieum.controller;


import com.goormcoder.ieum.dto.request.PlanCreateDto;
import com.goormcoder.ieum.dto.request.PlanMemberCreateDto;
import com.goormcoder.ieum.dto.response.DestinationFindDto;
import com.goormcoder.ieum.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/plans")
@Tag(name = "Plan", description = "일정 관련 API")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @GetMapping
    @Operation(summary = "여행지 목록 조회", description = "여행지 목록을 조회합니다.")
    public ResponseEntity<List<DestinationFindDto>> getAllDestination() {
        return ResponseEntity.status(HttpStatus.OK).body(planService.getAllDestination());
    }

    @PostMapping
    @Operation(summary = "일정 생성", description = "일정을 생성합니다. 이동수단(vehicle) 유형 - PUBLIC_TRANSPORTATION 또는 OWN_CAR")
    public ResponseEntity<String> createPlan(@Valid @RequestBody PlanCreateDto planCreateDto) {
        UUID memberId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        planService.createPlan(planCreateDto, memberId);
        return ResponseEntity.status(HttpStatus.OK).body("일정이 생성되었습니다.");
    }

    @PostMapping("/{planId}/invite")
    @Operation(summary = "멤버 추가", description = "일정 멤버를 추가합니다.")
    public ResponseEntity<String> createPlan(@PathVariable Long planId, @RequestBody PlanMemberCreateDto planMemberCreateDto) {
        planService.createPlanMember(planId, planMemberCreateDto.memberLoginIds());
        return ResponseEntity.status(HttpStatus.OK).body("멤버가 추가되었습니다.");
    }

}
