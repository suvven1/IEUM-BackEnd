package com.goormcoder.ieum.service;

import com.goormcoder.ieum.domain.Destination;
import com.goormcoder.ieum.domain.Member;
import com.goormcoder.ieum.domain.Plan;
import com.goormcoder.ieum.domain.PlanMember;
import com.goormcoder.ieum.dto.request.PlanCreateDto;
import com.goormcoder.ieum.dto.response.DestinationFindDto;
import com.goormcoder.ieum.exception.ErrorMessages;
import com.goormcoder.ieum.repository.DestinationRepository;
import com.goormcoder.ieum.repository.PlanRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final DestinationRepository destinationRepository;
    private final MemberService memberService;

    @Transactional
    public List<DestinationFindDto> getAllDestination() {
        return DestinationFindDto.listOf(destinationRepository.findAll());
    }

    @Transactional
    public void createPlan(PlanCreateDto dto, UUID memberId) {
        Member member = memberService.findById(memberId);
        Destination destination = destinationRepository.findById(dto.destinationId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.DESTINATION_NOT_FOUND.getMessage()));
        Plan plan = Plan.of(destination, dto.startedAt(), dto.endedAt(), dto.vehicle());
        plan.addPlanMember(PlanMember.of(plan, member));
        planRepository.save(plan);
    }
    
    public Plan findByPlanId(Long planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.PLAN_NOT_FOUND.getMessage()));
    }

}
