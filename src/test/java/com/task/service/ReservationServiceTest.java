package com.task.service;

import com.task.dto.ReservationRequest;
import com.task.dto.ReservationResponse;
import com.task.entity.Resource;
import com.task.entity.User;
import com.task.enums.Role;
import com.task.exception.BadRequestException;
import com.task.repository.ReservationRepository;
import com.task.repository.ResourceRepository;
import com.task.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationService reservationService;

    private User testUser;
    private Resource testResource;

    @BeforeEach
    public void setup() {
        testUser = new User(1L, "user", "password", "user@test.com", Role.ROLE_USER);
        testResource = new Resource(1L, "Conference Room", "Desc", new BigDecimal("50.00"), true);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "user", "password", java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void testCreateReservationSuccess() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);
        ReservationRequest request = new ReservationRequest(1L, start, end);

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(testUser));
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(testResource));
        when(reservationRepository.save(any())).thenAnswer(invocation -> {
            com.task.entity.Reservation r = invocation.getArgument(0);
            r.setId(100L);
            return r;
        });

        ReservationResponse response = reservationService.createReservation(request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("user", response.getUsername());
        assertEquals(new BigDecimal("100.00"), response.getTotalPrice());
    }

    @Test
    public void testCreateReservationEndTimeBeforeStartTime() {
        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end = start.minusHours(1);
        ReservationRequest request = new ReservationRequest(1L, start, end);

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(testUser));
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(testResource));

        assertThrows(BadRequestException.class, () -> reservationService.createReservation(request));
    }
}
