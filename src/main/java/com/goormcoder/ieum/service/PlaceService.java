package com.goormcoder.ieum.service;


import com.goormcoder.ieum.domain.*;
import com.goormcoder.ieum.dto.request.PlaceCreateDto;
import com.goormcoder.ieum.dto.request.PlaceShareDto;
import com.goormcoder.ieum.dto.request.PlaceVisitTimeUpdateDto;
import com.goormcoder.ieum.dto.response.PlaceFindDto;
import com.goormcoder.ieum.dto.response.PlaceInfoDto;
import com.goormcoder.ieum.exception.ConflictException;
import com.goormcoder.ieum.exception.ErrorMessages;
import com.goormcoder.ieum.repository.CategoryRepository;
import com.goormcoder.ieum.repository.PlaceRepository;
import com.goormcoder.ieum.repository.PlanRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final PlanRepository planRepository;
    private final CategoryRepository categoryRepository;

    private final MemberService memberService;
    private final PlanService planService;

    @Transactional
    public PlaceInfoDto createPlace(Long planId, UUID memberId, PlaceCreateDto dto) {
        Member member = memberService.findById(memberId);
        Plan plan = planService.findByPlanId(planId);
        Category category = findByCategoryId(dto.categoryId());

        validatePlanMember(plan, member);
        validateDuplicatePlace(plan, member, dto.placeName());

        Place place = Place.of(plan, member, null, null, dto.placeName(), dto.address(), category);
        plan.addPlace(place);
        planRepository.save(plan);

        return PlaceInfoDto.of(findByPlaceNameAndMember(dto.placeName(), member, plan));
    }

    @Transactional
    public PlaceFindDto sharePlace(PlaceShareDto dto) {
        // memberService.findById(memberId); - 검증 추가 예정
        // validatePlanMember(plan, member);
        Place place = findPlaceById(dto.placeId());
        place.marksActivatedAt();

        Plan plan = planService.findByPlanId(dto.planId());
        plan.addPlace(place);
        planRepository.save(plan);

        return PlaceFindDto.of(place);
    }

    @Transactional
    public void updateVisitTime(Long planId, Long placeId, UUID memberId, PlaceVisitTimeUpdateDto dto) {
        Member member = memberService.findById(memberId);
        Plan plan = planService.findByPlanId(planId);
        validatePlanMember(plan, member);
        validatePlaceVisitTimeUpdateDto(dto, plan);

        Place place = findPlaceById(placeId);
        if(!place.isActive()) {
            throw new IllegalArgumentException(ErrorMessages.BAD_REQUEST_PLACE_NOT_ACTIVE.getMessage());
        }

        place.marksStartedAt(dto.startedAt());
        place.marksEndedAt(dto.endedAt());
        placeRepository.save(place);
    }

    public List<Place> findAllPlaces() {
        return placeRepository.findAll();
    }

    public Place updatePlace(Long id, Place updatedPlace) {
        return updatedPlace;
//        return placeRepository.findById(id)
//                .map(place -> {
//                    place.setDeletedAt(updatedPlace.getDeletedAt());
//                    return placeRepository.save(place);
//                })
//                .orElseGet(() -> {
//                    updatedPlace.setId(id);
//                    return placeRepository.save(updatedPlace);
//                });
    }

    public void deletePlace(Long id) {
        placeRepository.deleteById(id);
    }

    private Category findByCategoryId(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.CATEGORY_NOT_FOUND.getMessage()));
    }

    private Place findPlaceById(Long placeId) {
        return placeRepository.findById(placeId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.PLACE_NOT_FOUND.getMessage()));
    }

    private Place findByPlaceNameAndMember(String placeName, Member member, Plan plan) {
        return placeRepository.findByPlaceNameAndMemberAndPlan(placeName, member, plan);
    }

    private void validateDuplicatePlace(Plan plan, Member member, String placeName) {
        if(placeRepository.existsByPlaceNameAndMemberAndPlan(placeName, member, plan)) {
            throw new ConflictException(ErrorMessages.PLACE_CONFLICT);
        }
    }

    private void validatePlanMember(Plan plan, Member member) {
        plan.getPlanMembers().stream()
                .filter(planMember -> planMember.getMember().equals(member))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.PLAN_MEMBER_NOT_FOUND.getMessage()));
    }

    private void validatePlaceVisitTimeUpdateDto(PlaceVisitTimeUpdateDto dto, Plan plan) {
        if(dto.startedAt().isBefore(plan.getStartedAt()) || dto.endedAt().isAfter(plan.getEndedAt())) {
            throw new IllegalArgumentException(ErrorMessages.BAD_REQUEST_PLACE_VISIT_TIME.getMessage());
        }
    }

}
