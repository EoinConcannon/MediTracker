package com.meditracker.vitalsservice;

import tools.jackson.databind.ObjectMapper;
import com.meditracker.vitalsservice.model.VitalReading;
import com.meditracker.vitalsservice.model.VitalType;
import com.meditracker.vitalsservice.service.VitalsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class VitalsControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private VitalsService vitalsService;

	private VitalReading buildBloodPressureReading(boolean alert) {
		VitalReading reading = new VitalReading();
		reading.setId(1L);
		reading.setPatientId(1L);
		reading.setVitalType(VitalType.BLOOD_PRESSURE);
		reading.setSystolic(alert ? 155 : 120);
		reading.setDiastolic(alert ? 95 : 80);
		reading.setAlertTriggered(alert);
		reading.setTimestamp(LocalDateTime.now());
		return reading;
	}

	private VitalReading buildHeartRateReading(boolean alert) {
		VitalReading reading = new VitalReading();
		reading.setId(2L);
		reading.setPatientId(1L);
		reading.setVitalType(VitalType.HEART_RATE);
		reading.setValue(alert ? 115.0 : 75.0);
		reading.setAlertTriggered(alert);
		reading.setTimestamp(LocalDateTime.now());
		return reading;
	}

	// ─── POST /api/vitals ────────────────────────────────────────────────────

	@Test
	void submitReading_NormalBloodPressure_Returns201NoAlert() throws Exception {
		VitalReading reading = buildBloodPressureReading(false);

		when(vitalsService.submitReading(any(VitalReading.class))).thenReturn(reading);

		mockMvc.perform(post("/api/vitals").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(reading))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.alertTriggered").value(false))
				.andExpect(jsonPath("$.vitalType").value("BLOOD_PRESSURE"));
	}

	@Test
	void submitReading_AbnormalBloodPressure_Returns201WithAlert() throws Exception {
		VitalReading reading = buildBloodPressureReading(true);

		when(vitalsService.submitReading(any(VitalReading.class))).thenReturn(reading);

		mockMvc.perform(post("/api/vitals").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(reading))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.alertTriggered").value(true));
	}

	@Test
	void submitReading_MissingVitalType_Returns400() throws Exception {
		VitalReading reading = buildBloodPressureReading(false);
		reading.setVitalType(null);

		mockMvc.perform(post("/api/vitals").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(reading))).andExpect(status().isBadRequest());
	}

	@Test
	void submitReading_MissingPatientId_Returns400() throws Exception {
		VitalReading reading = buildBloodPressureReading(false);
		reading.setPatientId(null);

		mockMvc.perform(post("/api/vitals").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(reading))).andExpect(status().isBadRequest());
	}

	@Test
	void submitReading_BloodPressureMissingSystolic_Returns400() throws Exception {
		when(vitalsService.submitReading(any(VitalReading.class))).thenThrow(
				new IllegalArgumentException("Systolic and diastolic values are required for blood pressure"));

		VitalReading reading = buildBloodPressureReading(false);
		reading.setSystolic(null);

		mockMvc.perform(post("/api/vitals").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(reading))).andExpect(status().isBadRequest());
	}

	@Test
	void submitReading_NormalHeartRate_Returns201() throws Exception {
		VitalReading reading = buildHeartRateReading(false);

		when(vitalsService.submitReading(any(VitalReading.class))).thenReturn(reading);

		mockMvc.perform(post("/api/vitals").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(reading))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.vitalType").value("HEART_RATE"))
				.andExpect(jsonPath("$.alertTriggered").value(false));
	}

	// ─── GET /api/vitals ─────────────────────────────────────────────────────

	@Test
	void getReadings_ByPatientId_Returns200WithList() throws Exception {
		VitalReading r1 = buildBloodPressureReading(false);
		VitalReading r2 = buildHeartRateReading(false);

		when(vitalsService.getReadingsByPatient(1L)).thenReturn(List.of(r1, r2));

		mockMvc.perform(get("/api/vitals").param("patientId", "1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	void getReadings_ByPatientIdAndType_ReturnsFiltered() throws Exception {
		VitalReading reading = buildBloodPressureReading(false);

		when(vitalsService.getReadingsByPatientAndType(1L, VitalType.BLOOD_PRESSURE)).thenReturn(List.of(reading));

		mockMvc.perform(get("/api/vitals").param("patientId", "1").param("vitalType", "BLOOD_PRESSURE"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].vitalType").value("BLOOD_PRESSURE"));
	}

	@Test
	void getReadings_NoReadings_Returns200EmptyList() throws Exception {
		when(vitalsService.getReadingsByPatient(99L)).thenReturn(List.of());

		mockMvc.perform(get("/api/vitals").param("patientId", "99")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void getReadings_InvalidVitalType_Returns400() throws Exception {
		mockMvc.perform(get("/api/vitals").param("patientId", "1").param("vitalType", "INVALID_TYPE"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void getReadings_AlertReadings_ShowAlertFlag() throws Exception {
		VitalReading alertReading = buildBloodPressureReading(true);

		when(vitalsService.getReadingsByPatient(1L)).thenReturn(List.of(alertReading));

		mockMvc.perform(get("/api/vitals").param("patientId", "1")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].alertTriggered").value(true));
	}
}