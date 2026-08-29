package com.task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.dto.ReservationRequest;
import com.task.enums.ReservationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void testUserCreateReservation() throws Exception {
        LocalDateTime now = LocalDateTime.now().plusHours(1);
        LocalDateTime later = now.plusHours(2);

        ReservationRequest request = new ReservationRequest(1L, now, later);

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalPrice").exists());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void testReservationInvalidTime() throws Exception {
        LocalDateTime now = LocalDateTime.now().plusHours(2);
        LocalDateTime earlier = now.minusHours(1);

        ReservationRequest request = new ReservationRequest(1L, now, earlier);

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void testUserGetFilteredReservations() throws Exception {
        mockMvc.perform(get("/reservations")
                        .param("status", ReservationStatus.PENDING.name())
                        .param("minPrice", "10.00")
                        .param("maxPrice", "500.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
