package com.meditracker.patientservice;

import tools.jackson.databind.ObjectMapper;
import com.meditracker.patientservice.dto.LoginRequest;
import com.meditracker.patientservice.exception.DuplicateEmailException;
import com.meditracker.patientservice.exception.InvalidCredentialsException;
import com.meditracker.patientservice.exception.ResourceNotFoundException;
import com.meditracker.patientservice.model.Doctor;
import com.meditracker.patientservice.service.DoctorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class DoctorControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private DoctorService doctorService;

	private Doctor buildDoctor() {
		Doctor doctor = new Doctor();
		doctor.setId(1L);
		doctor.setName("Dr. Sarah Murphy");
		doctor.setEmail("sarah.murphy@meditracker.com");
		doctor.setSpecialisation("Cardiology");
		doctor.setPassword("password123");
		return doctor;
	}

	// ─── POST /api/doctors ───────────────────────────────────────────────────

	@Test
	void registerDoctor_ValidRequest_Returns201() throws Exception {
		Doctor doctor = buildDoctor();

		when(doctorService.registerDoctor(any(Doctor.class))).thenReturn(doctor);

		mockMvc.perform(post("/api/doctors").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(doctor))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Dr. Sarah Murphy"))
				.andExpect(jsonPath("$.email").value("sarah.murphy@meditracker.com"))
				.andExpect(jsonPath("$.id").value(1));
	}

	@Test
	void registerDoctor_MissingName_Returns400() throws Exception {
		Doctor doctor = buildDoctor();
		doctor.setName(null);

		mockMvc.perform(post("/api/doctors").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(doctor))).andExpect(status().isBadRequest());
	}

	@Test
	void registerDoctor_MissingEmail_Returns400() throws Exception {
		Doctor doctor = buildDoctor();
		doctor.setEmail(null);

		mockMvc.perform(post("/api/doctors").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(doctor))).andExpect(status().isBadRequest());
	}

	@Test
	void registerDoctor_MissingSpecialisation_Returns400() throws Exception {
		Doctor doctor = buildDoctor();
		doctor.setSpecialisation(null);

		mockMvc.perform(post("/api/doctors").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(doctor))).andExpect(status().isBadRequest());
	}

	@Test
	void registerDoctor_DuplicateEmail_Returns409() throws Exception {
		Doctor doctor = buildDoctor();

		when(doctorService.registerDoctor(any(Doctor.class)))
				.thenThrow(new DuplicateEmailException("A doctor with this email already exists"));

		mockMvc.perform(post("/api/doctors").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(doctor))).andExpect(status().isConflict())
				.andExpect(jsonPath("$.error").value("A doctor with this email already exists"));
	}

	// ─── GET /api/doctors/{id} ───────────────────────────────────────────────

	@Test
	void getDoctorById_ValidId_Returns200() throws Exception {
		Doctor doctor = buildDoctor();

		when(doctorService.getDoctorById(1L)).thenReturn(doctor);

		mockMvc.perform(get("/api/doctors/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Dr. Sarah Murphy"));
	}

	@Test
	void getDoctorById_NotFound_Returns404() throws Exception {
		when(doctorService.getDoctorById(999L))
				.thenThrow(new ResourceNotFoundException("Doctor not found with id: 999"));

		mockMvc.perform(get("/api/doctors/999")).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("Doctor not found with id: 999"));
	}

	// ─── GET /api/doctors ────────────────────────────────────────────────────

	@Test
	void getAllDoctors_Returns200WithList() throws Exception {
		Doctor d1 = buildDoctor();
		Doctor d2 = buildDoctor();
		d2.setId(2L);
		d2.setEmail("other@meditracker.com");

		when(doctorService.getAllDoctors()).thenReturn(List.of(d1, d2));

		mockMvc.perform(get("/api/doctors")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].name").value("Dr. Sarah Murphy"));
	}

	@Test
	void getAllDoctors_EmptyList_Returns200() throws Exception {
		when(doctorService.getAllDoctors()).thenReturn(List.of());

		mockMvc.perform(get("/api/doctors")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
	}

	// ─── POST /api/doctors/login ─────────────────────────────────────────────

	@Test
	void loginDoctor_ValidCredentials_Returns200() throws Exception {
		Doctor doctor = buildDoctor();
		LoginRequest request = new LoginRequest();
		request.setEmail("sarah.murphy@meditracker.com");
		request.setPassword("password123");

		when(doctorService.loginDoctor("sarah.murphy@meditracker.com", "password123")).thenReturn(doctor);

		mockMvc.perform(post("/api/doctors/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Dr. Sarah Murphy"))
				.andExpect(jsonPath("$.email").value("sarah.murphy@meditracker.com"));
	}

	@Test
	void loginDoctor_EmailNotFound_Returns404() throws Exception {
		LoginRequest request = new LoginRequest();
		request.setEmail("nobody@test.com");
		request.setPassword("password123");

		when(doctorService.loginDoctor("nobody@test.com", "password123"))
				.thenThrow(new ResourceNotFoundException("No account found with that email address"));

		mockMvc.perform(post("/api/doctors/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("No account found with that email address"));
	}

	@Test
	void loginDoctor_WrongPassword_Returns401() throws Exception {
		LoginRequest request = new LoginRequest();
		request.setEmail("sarah.murphy@meditracker.com");
		request.setPassword("wrongpassword");

		when(doctorService.loginDoctor("sarah.murphy@meditracker.com", "wrongpassword"))
				.thenThrow(new InvalidCredentialsException("Incorrect password"));

		mockMvc.perform(post("/api/doctors/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error").value("Incorrect password"));
	}
}