package com.task.service;

import com.task.dto.PagedResponse;
import com.task.dto.ReservationRequest;
import com.task.dto.ReservationResponse;
import com.task.dto.ReservationUpdateRequest;
import com.task.entity.Reservation;
import com.task.entity.Resource;
import com.task.entity.User;
import com.task.enums.ReservationStatus;
import com.task.enums.Role;
import com.task.exception.BadRequestException;
import com.task.exception.ResourceNotFoundException;
import com.task.exception.UnauthorizedException;
import com.task.repository.ReservationRepository;
import com.task.repository.ResourceRepository;
import com.task.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ResourceRepository resourceRepository,
                              UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    private User fetchCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }

    private void verifyOwnershipOrAdmin(User currentUser, Reservation reservation, String action) {
        if (!currentUser.getRole().equals(Role.ROLE_ADMIN) && !reservation.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Access Denied: Cannot " + action + " another user's reservation.");
        }
    }

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request) {
        User user = fetchCurrentUser();

        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + request.getResourceId()));

        if (!resource.isAvailable()) {
            throw new BadRequestException("Requested resource is currently unavailable.");
        }

        validateBookingWindow(request.getStartTime(), request.getEndTime());

        BigDecimal cost = request.getTotalPrice() != null && request.getTotalPrice().compareTo(BigDecimal.ZERO) > 0
                ? request.getTotalPrice()
                : computeCost(resource.getPricePerHour(), request.getStartTime(), request.getEndTime());

        Reservation booking = new Reservation();
        booking.setUser(user);
        booking.setResource(resource);
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setTotalPrice(cost);
        booking.setStatus(ReservationStatus.PENDING);

        return toDto(reservationRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id) {
        User currentUser = fetchCurrentUser();
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        verifyOwnershipOrAdmin(currentUser, reservation, "view");
        return toDto(reservation);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ReservationResponse> getReservations(ReservationStatus status,
                                                               BigDecimal minPrice,
                                                               BigDecimal maxPrice,
                                                               int page,
                                                               int size,
                                                               String sortBy,
                                                               String sortDir) {
        User currentUser = fetchCurrentUser();
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Reservation> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (!currentUser.getRole().equals(Role.ROLE_ADMIN)) {
                predicates.add(cb.equal(root.get("user").get("id"), currentUser.getId()));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("totalPrice"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("totalPrice"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Reservation> pageResult = reservationRepository.findAll(spec, pageable);
        List<ReservationResponse> content = pageResult.getContent().stream()
                .map(this::toDto)
                .toList();

        return new PagedResponse<>(
                content,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast()
        );
    }

    @Transactional
    public ReservationResponse updateReservation(Long id, ReservationUpdateRequest request) {
        User currentUser = fetchCurrentUser();
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        verifyOwnershipOrAdmin(currentUser, reservation, "update");

        if (request.getStartTime() != null || request.getEndTime() != null) {
            LocalDateTime start = request.getStartTime() != null ? request.getStartTime() : reservation.getStartTime();
            LocalDateTime end = request.getEndTime() != null ? request.getEndTime() : reservation.getEndTime();
            validateBookingWindow(start, end);
            reservation.setStartTime(start);
            reservation.setEndTime(end);
            reservation.setTotalPrice(computeCost(reservation.getResource().getPricePerHour(), start, end));
        }

        if (request.getStatus() != null) {
            reservation.setStatus(request.getStatus());
        }

        return toDto(reservationRepository.save(reservation));
    }

    @Transactional
    public void deleteReservation(Long id) {
        User currentUser = fetchCurrentUser();
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        verifyOwnershipOrAdmin(currentUser, reservation, "delete");
        reservationRepository.delete(reservation);
    }

    private void validateBookingWindow(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new BadRequestException("Start and end timestamps are required.");
        }
        if (!end.isAfter(start)) {
            throw new BadRequestException("End time must be strictly after start time.");
        }
    }

    private BigDecimal computeCost(BigDecimal hourlyRate, LocalDateTime start, LocalDateTime end) {
        long minutes = Duration.between(start, end).toMinutes();
        double hours = Math.max(1.0, minutes / 60.0);
        return hourlyRate.multiply(BigDecimal.valueOf(hours)).setScale(2, RoundingMode.HALF_UP);
    }

    private ReservationResponse toDto(Reservation r) {
        return new ReservationResponse(
                r.getId(),
                r.getUser().getId(),
                r.getUser().getUsername(),
                r.getResource().getId(),
                r.getResource().getName(),
                r.getStartTime(),
                r.getEndTime(),
                r.getTotalPrice(),
                r.getStatus(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
