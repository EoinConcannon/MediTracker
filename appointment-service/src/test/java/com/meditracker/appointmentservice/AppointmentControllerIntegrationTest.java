package com.meditracker.appointmentservice;

import tools.jackson.databind.ObjectMapper;
import com.meditracker.appointmentservice.exception.AppointmentConflictException;
import com.meditracker.appointmentservice.exception.ResourceNotFoundException;
import com.meditracker.appointmentservice.model.Appointment;
import com.meditracker.appointmentservice.model.AppointmentStatus;
import com.meditracker.appointmentservice.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AppointmentControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private AppointmentService appointmentService;

	private Appointment buildAppointment() {
		Appointment appointment = new Appointment();
		appointment.setId(1L);
		appointment.setPatientId(1L);
		appointment.setDoctorId(1L);
		appointment.setDate(LocalDate.now().plusDays(7));
		appointment.setTime(LocalTime.of(10, 0));
		appointment.setNotes("General check-up");
		appointment.setStatus(AppointmentStatus.PENDING);
		return appointment;
	}

	// ─── POST /api/appointments ──────────────────────────────────────────────

	@Test
	void bookAppointment_ValidRequest_Returns201() throws Exception {
		Appointment appointment = buildAppointment();

		when(appointmentService.bookAppointment(any(Appointment.class))).thenReturn(appointment);

		mockMvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(appointment))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("PENDING")).andExpect(jsonPath("$.patientId").value(1))
				.andExpect(jsonPath("$.doctorId").value(1));
	}

	@Test
	void bookAppointment_PastDate_Returns400() throws Exception {
		Appointment appointment = buildAppointment();

		when(appointmentService.bookAppointment(any(Appointment.class)))
				.thenThrow(new IllegalArgumentException("Appointment date cannot be in the past"));

		mockMvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(appointment))).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("Appointment date cannot be in the past"));
	}

	@Test
	void bookAppointment_DoctorConflict_Returns409() throws Exception {
		Appointment appointment = buildAppointment();

		when(appointmentService.bookAppointment(any(Appointment.class)))
				.thenThrow(new AppointmentConflictException("Doctor already has an appointment at this date and time"));

		mockMvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(appointment))).andExpect(status().isConflict())
				.andExpect(jsonPath("$.error").value("Doctor already has an appointment at this date and time"));
	}

	@Test
	void bookAppointment_PatientConflict_Returns409() throws Exception {
		Appointment appointment = buildAppointment();

		when(appointmentService.bookAppointment(any(Appointment.class)))
				.thenThrow(new AppointmentConflictException("You already have an appointment at this date and time"));

		mockMvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(appointment))).andExpect(status().isConflict())
				.andExpect(jsonPath("$.error").value("You already have an appointment at this date and time"));
	}

	@Test
	void bookAppointment_MissingPatientId_Returns400() throws Exception {
		Appointment appointment = buildAppointment();
		appointment.setPatientId(null);

		mockMvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(appointment))).andExpect(status().isBadRequest());
	}

	@Test
	void bookAppointment_MissingDoctorId_Returns400() throws Exception {
		Appointment appointment = buildAppointment();
		appointment.setDoctorId(null);

		mockMvc.perform(post("/api/appointments").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(appointment))).andExpect(status().isBadRequest());
	}

	// ─── GET /api/appointments ───────────────────────────────────────────────

	@Test
	void getAppointments_ByPatientId_Returns200() throws Exception {
		Appointment appointment = buildAppointment();

		when(appointmentService.getAppointmentsByPatient(1L)).thenReturn(List.of(appointment));

		mockMvc.perform(get("/api/appointments").param("patientId", "1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].patientId").value(1));
	}

	@Test
	void getAppointments_ByDoctorId_Returns200() throws Exception {
		Appointment appointment = buildAppointment();

		when(appointmentService.getAppointmentsByDoctor(1L)).thenReturn(List.of(appointment));

		mockMvc.perform(get("/api/appointments").param("doctorId", "1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));
	}

	@Test
	void getAppointments_ByPatientUpcoming_Returns200() throws Exception {
		Appointment appointment = buildAppointment();
		appointment.setStatus(AppointmentStatus.SCHEDULED);

		when(appointmentService.getUpcomingAppointmentsByPatient(1L)).thenReturn(List.of(appointment));

		mockMvc.perform(get("/api/appointments").param("patientId", "1").param("upcoming", "true"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
	}

	@Test
	void getAppointments_NoParams_Returns400() throws Exception {
		mockMvc.perform(get("/api/appointments")).andExpect(status().isBadRequest());
	}

	@Test
	void getAppointments_EmptyList_Returns200() throws Exception {
		when(appointmentService.getAppointmentsByPatient(99L)).thenReturn(List.of());

		mockMvc.perform(get("/api/appointments").param("patientId", "99")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	// ─── PATCH /api/appointments/{id}/confirm ────────────────────────────────

	@Test
	void confirmAppointment_Success_Returns200() throws Exception {
		Appointment appointment = buildAppointment();
		appointment.setStatus(AppointmentStatus.SCHEDULED);

		when(appointmentService.confirmAppointment(1L)).thenReturn(appointment);

		mockMvc.perform(patch("/api/appointments/1/confirm")).andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SCHEDULED"));
	}

	@Test
	void confirmAppointment_NotPending_Returns400() throws Exception {
		when(appointmentService.confirmAppointment(1L))
				.thenThrow(new IllegalArgumentException("Only pending appointments can be confirmed"));

		mockMvc.perform(patch("/api/appointments/1/confirm")).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("Only pending appointments can be confirmed"));
	}

	@Test
	void confirmAppointment_NotFound_Returns404() throws Exception {
		when(appointmentService.confirmAppointment(999L))
				.thenThrow(new ResourceNotFoundException("Appointment not found with id: 999"));

		mockMvc.perform(patch("/api/appointments/999/confirm")).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("Appointment not found with id: 999"));
	}

	// ─── PATCH /api/appointments/{id}/cancel ─────────────────────────────────

	@Test
	void cancelAppointment_Success_Returns200() throws Exception {
		Appointment appointment = buildAppointment();
		appointment.setStatus(AppointmentStatus.CANCELLED);

		when(appointmentService.cancelAppointment(1L)).thenReturn(appointment);

		mockMvc.perform(patch("/api/appointments/1/cancel")).andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CANCELLED"));
	}

	@Test
	void cancelAppointment_AlreadyCompleted_Returns400() throws Exception {
		when(appointmentService.cancelAppointment(1L))
				.thenThrow(new IllegalArgumentException("Cannot cancel a completed appointment"));

		mockMvc.perform(patch("/api/appointments/1/cancel")).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("Cannot cancel a completed appointment"));
	}

	@Test
	void cancelAppointment_AlreadyCancelled_Returns400() throws Exception {
		when(appointmentService.cancelAppointment(1L))
				.thenThrow(new IllegalArgumentException("Appointment is already cancelled"));

		mockMvc.perform(patch("/api/appointments/1/cancel")).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("Appointment is already cancelled"));
	}

	@Test
	void cancelAppointment_NotFound_Returns404() throws Exception {
		when(appointmentService.cancelAppointment(999L))
				.thenThrow(new ResourceNotFoundException("Appointment not found with id: 999"));

		mockMvc.perform(patch("/api/appointments/999/cancel")).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("Appointment not found with id: 999"));
	}

	// ─── GET /api/appointments/pending ──────────────────────────────────────

	@Test
	void getPendingAppointments_Returns200() throws Exception {
		Appointment appointment = buildAppointment();

		when(appointmentService.getPendingAppointmentsByDoctor(1L)).thenReturn(List.of(appointment));

		mockMvc.perform(get("/api/appointments/pending").param("doctorId", "1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].status").value("PENDING"));
	}

	@Test
	void getPendingAppointments_NoPending_Returns200Empty() throws Exception {
		when(appointmentService.getPendingAppointmentsByDoctor(1L)).thenReturn(List.of());

		mockMvc.perform(get("/api/appointments/pending").param("doctorId", "1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}
}