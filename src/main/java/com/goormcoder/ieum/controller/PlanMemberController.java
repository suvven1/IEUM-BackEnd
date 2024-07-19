package com.goormcoder.ieum.controller;

import com.goormcoder.ieum.domain.enumeration.InviteAcceptance;
import com.goormcoder.ieum.dto.request.PlanMemberCreateDto;
import com.goormcoder.ieum.dto.response.InviteFindAllDto;
import com.goormcoder.ieum.dto.response.InviteResultDto;
import com.goormcoder.ieum.service.PlanMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/plan/members")
@Tag(name = "Plan Member", description = "일정 멤버 관련 API")
@RequiredArgsConstructor
public class PlanMemberController {

    private final PlanMemberService planMemberService;

    @PostMapping("/invite/{planId}")
    @Operation(summary = "멤버 초대", description = "일정에 멤버를 초대합니다.")
    public ResponseEntity<InviteResultDto> invitePlanMember(@PathVariable Long planId, @RequestBody PlanMemberCreateDto planMemberCreateDto) {
        UUID memberId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        InviteResultDto inviteResult = planMemberService.invitePlanMember(memberId, planId, planMemberCreateDto.memberLoginIds());
        return ResponseEntity.status(HttpStatus.OK).body(inviteResult);
    }

    @GetMapping("/invite/{planId}")
    @Operation(summary = "보낸 초대 조회", description = "보낸 초대를 조회합니다. (해당 일정의 모든 초대를 조회)")
    public ResponseEntity<List<InviteFindAllDto>> findAllInviteByPlanId(@PathVariable Long planId) {
        UUID memberId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        return ResponseEntity.status(HttpStatus.OK).body(planMemberService.findAllInviteById(memberId, planId));
    }

    @GetMapping("/invite")
    @Operation(summary = "받은 초대 조회", description = "받은 초대를 조회합니다.")
    public ResponseEntity<List<InviteFindAllDto>> findAllInviteByMemberId() {
        UUID memberId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        return ResponseEntity.status(HttpStatus.OK).body(planMemberService.findAllInviteById(memberId));
    }

    @PatchMapping("/invite/{planId}/{acceptance}")
    @Operation(summary = "멤버 초대 응답", description = "초대를 수락 또는 거절 합니다.")
    public ResponseEntity<String> responseToInvite(@PathVariable Long planId, @PathVariable InviteAcceptance acceptance) {
        UUID memberId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        planMemberService.responseToInvite(memberId, planId, acceptance);
        return ResponseEntity.status(HttpStatus.OK).body("[" + acceptance + "] 응답이 완료되었습니다.");
    }

    @DeleteMapping("/invite/cancel/{planId}/{loginId}")
    @Operation(summary = "멤버 초대 취소", description = "보낸 초대를 취소합니다. (상대가 응답하기 전에만 가능)")
    public ResponseEntity<String> cancelInvite(@PathVariable Long planId, @PathVariable String loginId) {
        UUID memberId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        planMemberService.cancelPlanMemberInvite(memberId, loginId, planId);
        return ResponseEntity.status(HttpStatus.OK).body("초대 취소가 완료되었습니다.");
    }


}
