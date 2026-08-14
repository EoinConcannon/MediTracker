package com.meditracker.patientservice;

import tools.jackson.databind.ObjectMapper;
import com.meditracker.patientservice.model.Prescription;
import com.meditracker.patientservice.model.PrescriptionStatus;
import com.meditracker.patientservice.service.PrescriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
public class PrescriptionControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private PrescriptionService prescriptionService;

	private Prescription buildPrescription() {
		Prescription prescription = new Prescription();
		prescription.setId(1L);
		prescription.setPatientId(1L);
		prescription.setDoctorId(1L);
		prescription.setDrugName("Lisinopril");
		prescription.setDosage("10mg");
		prescription.setFrequency("Once daily");
		prescription.setStartDate(LocalDate.now());
		prescription.setStatus(PrescriptionStatus.ACTIVE);
		prescription.setAllergyWarning(false);
		return prescription;
	}

	// ─── POST /api/prescriptions ─────────────────────────────────────────────

	@Test
	void issuePrescription_ValidRequest_Returns201() throws Exception {
		Prescription prescription = buildPrescription();

		when(prescriptionService.issuePrescription(any(Prescription.class))).thenReturn(prescription);

		mockMvc.perform(post("/api/prescriptions").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(prescription))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.drugName").value("Lisinopril")).andExpect(jsonPath("$.dosage").value("10mg"))
				.andExpect(jsonPath("$.status").value("ACTIVE")).andExpect(jsonPath("$.allergyWarning").value(false));
	}

	@Test
	void issuePrescription_AllergyConflict_Returns201WithWarning() throws Exception {
		Prescription prescription = buildPrescription();
		prescription.setDrugName("Penicillin");
		prescription.setAllergyWarning(true);

		when(prescriptionService.issuePrescription(any(Prescription.class))).thenReturn(prescription);

		mockMvc.perform(post("/api/prescriptions").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(prescription))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.allergyWarning").value(true));
	}

	@Test
	void issuePrescription_MissingDrugName_Returns400() throws Exception {
		Prescription prescription = buildPrescription();
		prescription.setDrugName(null);

		mockMvc.perform(post("/api/prescriptions").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(prescription))).andExpect(status().isBadRequest());
	}

	@Test
	void issuePrescription_MissingDosage_Returns400() throws Exception {
		Prescription prescription = buildPrescription();
		prescription.setDosage(null);

		mockMvc.perform(post("/api/prescriptions").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(prescription))).andExpect(status().isBadRequest());
	}

	@Test
	void issuePrescription_MissingFrequency_Returns400() throws Exception {
		Prescription prescription = buildPrescription();
		prescription.setFrequency(null);

		mockMvc.perform(post("/api/prescriptions").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(prescription))).andExpect(status().isBadRequest());
	}

	@Test
	void issuePrescription_MissingStartDate_Returns400() throws Exception {
		Prescription prescription = buildPrescription();
		prescription.setStartDate(null);

		mockMvc.perform(post("/api/prescriptions").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(prescription))).andExpect(status().isBadRequest());
	}

	@Test
	void issuePrescription_EndDateBeforeStartDate_Returns400() throws Exception {
		Prescription prescription = buildPrescription();

		when(prescriptionService.issuePrescription(any(Prescription.class)))
				.thenThrow(new IllegalArgumentException("End date cannot be before start date"));

		mockMvc.perform(post("/api/prescriptions").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(prescription))).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("End date cannot be before start date"));
	}

	// ─── GET /api/prescriptions ──────────────────────────────────────────────

	@Test
	void getPrescriptions_ByPatientId_Returns200() throws Exception {
		Prescription prescription = buildPrescription();

		when(prescriptionService.getPrescriptionsByPatient(1L)).thenReturn(List.of(prescription));

		mockMvc.perform(get("/api/prescriptions").param("patientId", "1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].drugName").value("Lisinopril"));
	}

	@Test
	void getPrescriptions_ByPatientIdAndActiveStatus_Returns200() throws Exception {
		Prescription prescription = buildPrescription();

		when(prescriptionService.getActivePrescriptionsByPatient(1L)).thenReturn(List.of(prescription));

		mockMvc.perform(get("/api/prescriptions").param("patientId", "1").param("status", "ACTIVE"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].status").value("ACTIVE"));
	}

	@Test
	void getPrescriptions_ByDoctorId_Returns200() throws Exception {
		Prescription prescription = buildPrescription();

		when(prescriptionService.getPrescriptionsByDoctor(1L)).thenReturn(List.of(prescription));

		mockMvc.perform(get("/api/prescriptions").param("doctorId", "1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));
	}

	@Test
	void getPrescriptions_NoParams_Returns400() throws Exception {
		mockMvc.perform(get("/api/prescriptions")).andExpect(status().isBadRequest());
	}

	@Test
	void getPrescriptions_EmptyList_Returns200() throws Exception {
		when(prescriptionService.getPrescriptionsByPatient(99L)).thenReturn(List.of());

		mockMvc.perform(get("/api/prescriptions").param("patientId", "99")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}
}