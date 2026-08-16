package com.meditracker.patientservice;

import tools.jackson.databind.ObjectMapper;
import com.meditracker.patientservice.dto.LoginRequest;
import com.meditracker.patientservice.exception.DuplicateEmailException;
import com.meditracker.patientservice.exception.InvalidCredentialsException;
import com.meditracker.patientservice.exception.ResourceNotFoundException;
import com.meditracker.patientservice.exception.UnauthorisedAccessException;
import com.meditracker.patientservice.model.Patient;
import com.meditracker.patientservice.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PatientControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private PatientService patientService;

	private Patient buildPatient() {
		Patient patient = new Patient();
		patient.setId(1L);
		patient.setName("Séan O'Brien");
		patient.setEmail("sean.obrien@email.ie");
		patient.setDateOfBirth(LocalDate.of(1972, 5, 14));
		patient.setAssignedDoctorId(1L);
		patient.setAllergies("Penicillin");
		patient.setPassword("password123");
		return patient;
	}

	// ─── POST /api/patients ──────────────────────────────────────────────────

	@Test
	void registerPatient_ValidRequest_Returns201() throws Exception {
		Patient patient = buildPatient();

		when(patientService.registerPatient(any(Patient.class))).thenReturn(patient);

		mockMvc.perform(post("/api/patients").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(patient))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Séan O'Brien"))
				.andExpect(jsonPath("$.email").value("sean.obrien@email.ie")).andExpect(jsonPath("$.id").value(1));
	}

	@Test
	void registerPatient_MissingName_Returns400() throws Exception {
		Patient patient = buildPatient();
		patient.setName(null);

		mockMvc.perform(post("/api/patients").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(patient))).andExpect(status().isBadRequest());
	}

	@Test
	void registerPatient_MissingEmail_Returns400() throws Exception {
		Patient patient = buildPatient();
		patient.setEmail(null);

		mockMvc.perform(post("/api/patients").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(patient))).andExpect(status().isBadRequest());
	}

	@Test
	void registerPatient_FutureDateOfBirth_Returns400() throws Exception {
		Patient patient = buildPatient();
		patient.setDateOfBirth(LocalDate.now().plusYears(1));

		mockMvc.perform(post("/api/patients").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(patient))).andExpect(status().isBadRequest());
	}

	@Test
	void registerPatient_DuplicateEmail_Returns409() throws Exception {
		Patient patient = buildPatient();

		when(patientService.registerPatient(any(Patient.class)))
				.thenThrow(new DuplicateEmailException("A patient with this email already exists"));

		mockMvc.perform(post("/api/patients").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(patient))).andExpect(status().isConflict())
				.andExpect(jsonPath("$.error").value("A patient with this email already exists"));
	}

	@Test
	void registerPatient_DoctorNotFound_Returns404() throws Exception {
		Patient patient = buildPatient();

		when(patientService.registerPatient(any(Patient.class)))
				.thenThrow(new ResourceNotFoundException("Doctor not found with id: 1"));

		mockMvc.perform(post("/api/patients").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(patient))).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("Doctor not found with id: 1"));
	}

	// ─── GET /api/patients/{id} ──────────────────────────────────────────────

	@Test
	void getPatientById_NoDocktorId_Returns200() throws Exception {
		Patient patient = buildPatient();

		when(patientService.getPatientById(1L)).thenReturn(patient);

		mockMvc.perform(get("/api/patients/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Séan O'Brien"));
	}

	@Test
	void getPatientById_WithAuthorisedDoctorId_Returns200() throws Exception {
		Patient patient = buildPatient();

		when(patientService.getPatientByIdForDoctor(1L, 1L)).thenReturn(patient);

		mockMvc.perform(get("/api/patients/1").param("doctorId", "1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Séan O'Brien"));
	}

	@Test
	void getPatientById_UnauthorisedDoctor_Returns403() throws Exception {
		when(patientService.getPatientByIdForDoctor(1L, 99L))
				.thenThrow(new UnauthorisedAccessException("You are not authorised to view this patient"));

		mockMvc.perform(get("/api/patients/1").param("doctorId", "99")).andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error").value("You are not authorised to view this patient"));
	}

	@Test
	void getPatientById_NotFound_Returns404() throws Exception {
		when(patientService.getPatientById(999L))
				.thenThrow(new ResourceNotFoundException("Patient not found with id: 999"));

		mockMvc.perform(get("/api/patients/999")).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("Patient not found with id: 999"));
	}

	// ─── GET /api/patients ───────────────────────────────────────────────────

	@Test
	void getPatients_NoDoctorId_ReturnsAllPatients() throws Exception {
		Patient p1 = buildPatient();
		Patient p2 = buildPatient();
		p2.setId(2L);
		p2.setEmail("aoife.kelly@email.ie");

		when(patientService.getAllPatients()).thenReturn(List.of(p1, p2));

		mockMvc.perform(get("/api/patients")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	void getPatients_WithDoctorId_ReturnsFilteredList() throws Exception {
		Patient patient = buildPatient();

		when(patientService.getPatientsByDoctorId(1L)).thenReturn(List.of(patient));

		mockMvc.perform(get("/api/patients").param("doctorId", "1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].name").value("Séan O'Brien"));
	}

	@Test
	void getPatients_EmptyList_Returns200() throws Exception {
		when(patientService.getAllPatients()).thenReturn(List.of());

		mockMvc.perform(get("/api/patients")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
	}

	// ─── POST /api/patients/login ────────────────────────────────────────────

	@Test
	void loginPatient_ValidCredentials_Returns200() throws Exception {
		Patient patient = buildPatient();
		LoginRequest request = new LoginRequest();
		request.setEmail("sean.obrien@email.ie");
		request.setPassword("password123");

		when(patientService.loginPatient("sean.obrien@email.ie", "password123")).thenReturn(patient);

		mockMvc.perform(post("/api/patients/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Séan O'Brien"));
	}

	@Test
	void loginPatient_EmailNotFound_Returns404() throws Exception {
		LoginRequest request = new LoginRequest();
		request.setEmail("nobody@test.com");
		request.setPassword("password123");

		when(patientService.loginPatient("nobody@test.com", "password123"))
				.thenThrow(new ResourceNotFoundException("No account found with that email address"));

		mockMvc.perform(post("/api/patients/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("No account found with that email address"));
	}

	@Test
	void loginPatient_WrongPassword_Returns401() throws Exception {
		LoginRequest request = new LoginRequest();
		request.setEmail("sean.obrien@email.ie");
		request.setPassword("wrongpassword");

		when(patientService.loginPatient("sean.obrien@email.ie", "wrongpassword"))
				.thenThrow(new InvalidCredentialsException("Incorrect password"));

		mockMvc.perform(post("/api/patients/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error").value("Incorrect password"));
	}
}