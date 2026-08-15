package com.meditracker.vitalsservice;

import com.meditracker.vitalsservice.dto.PatientDTO;
import com.meditracker.vitalsservice.model.VitalReading;
import com.meditracker.vitalsservice.model.VitalType;
import com.meditracker.vitalsservice.repository.VitalReadingRepository;
import com.meditracker.vitalsservice.service.NotificationServiceClient;
import com.meditracker.vitalsservice.service.PatientServiceClient;
import com.meditracker.vitalsservice.service.ThresholdService;
import com.meditracker.vitalsservice.service.VitalsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VitalsServiceTest {

	@Mock
	private VitalReadingRepository vitalReadingRepository;

	@Mock
	private ThresholdService thresholdService;

	@Mock
	private PatientServiceClient patientServiceClient;

	@Mock
	private NotificationServiceClient notificationServiceClient;

	@InjectMocks
	private VitalsService vitalsService;

	private VitalReading buildBloodPressureReading() {
		VitalReading reading = new VitalReading();
		reading.setPatientId(1L);
		reading.setVitalType(VitalType.BLOOD_PRESSURE);
		reading.setSystolic(120);
		reading.setDiastolic(80);
		reading.setTimestamp(LocalDateTime.now());
		return reading;
	}

	private VitalReading buildHeartRateReading() {
		VitalReading reading = new VitalReading();
		reading.setPatientId(1L);
		reading.setVitalType(VitalType.HEART_RATE);
		reading.setValue(75.0);
		reading.setTimestamp(LocalDateTime.now());
		return reading;
	}

	private PatientDTO buildPatientDTO() {
		PatientDTO patient = new PatientDTO();
		patient.setId(1L);
		patient.setName("Séan O'Brien");
		patient.setAssignedDoctorId(1L);
		return patient;
	}

	// ─── submitReading ───────────────────────────────────────────────────────

	@Test
	void submitReading_NormalBloodPressure_SavesWithNoAlert() {
		VitalReading reading = buildBloodPressureReading();

		when(thresholdService.isAbnormal(reading)).thenReturn(false);
		when(vitalReadingRepository.save(reading)).thenReturn(reading);

		VitalReading result = vitalsService.submitReading(reading);

		assertNotNull(result);
		assertFalse(result.getAlertTriggered());
		verify(vitalReadingRepository, times(1)).save(reading);
		verify(notificationServiceClient, never()).sendAlert(any());
	}

	@Test
	void submitReading_AbnormalBloodPressure_SavesAndSendsAlert() {
		VitalReading reading = buildBloodPressureReading();
		reading.setSystolic(155);
		reading.setDiastolic(95);

		PatientDTO patient = buildPatientDTO();

		when(thresholdService.isAbnormal(reading)).thenReturn(true);
		when(thresholdService.formatReadingValue(reading)).thenReturn("155/95 mmHg");
		when(thresholdService.getSafeRange(VitalType.BLOOD_PRESSURE))
				.thenReturn("Systolic 90-139 mmHg, Diastolic 60-89 mmHg");
		when(vitalReadingRepository.save(reading)).thenReturn(reading);
		when(patientServiceClient.getPatient(1L)).thenReturn(patient);

		VitalReading result = vitalsService.submitReading(reading);

		assertTrue(result.getAlertTriggered());
		verify(notificationServiceClient, times(1)).sendAlert(any());
	}

	@Test
	void submitReading_NormalHeartRate_SavesWithNoAlert() {
		VitalReading reading = buildHeartRateReading();

		when(thresholdService.isAbnormal(reading)).thenReturn(false);
		when(vitalReadingRepository.save(reading)).thenReturn(reading);

		VitalReading result = vitalsService.submitReading(reading);

		assertFalse(result.getAlertTriggered());
		verify(notificationServiceClient, never()).sendAlert(any());
	}

	@Test
	void submitReading_AbnormalHeartRate_SendsAlert() {
		VitalReading reading = buildHeartRateReading();
		reading.setValue(115.0);

		PatientDTO patient = buildPatientDTO();

		when(thresholdService.isAbnormal(reading)).thenReturn(true);
		when(thresholdService.formatReadingValue(reading)).thenReturn("115.0 BPM");
		when(thresholdService.getSafeRange(VitalType.HEART_RATE)).thenReturn("60-100 BPM");
		when(vitalReadingRepository.save(reading)).thenReturn(reading);
		when(patientServiceClient.getPatient(1L)).thenReturn(patient);

		VitalReading result = vitalsService.submitReading(reading);

		assertTrue(result.getAlertTriggered());
		verify(notificationServiceClient, times(1)).sendAlert(any());
	}

	@Test
	void submitReading_BloodPressure_MissingSystolic_ThrowsException() {
		VitalReading reading = new VitalReading();
		reading.setPatientId(1L);
		reading.setVitalType(VitalType.BLOOD_PRESSURE);
		reading.setSystolic(null);
		reading.setDiastolic(80);

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> vitalsService.submitReading(reading));

		assertEquals("Systolic and diastolic values are required for blood pressure", ex.getMessage());
		verify(vitalReadingRepository, never()).save(any());
	}

	@Test
	void submitReading_BloodPressure_MissingDiastolic_ThrowsException() {
		VitalReading reading = new VitalReading();
		reading.setPatientId(1L);
		reading.setVitalType(VitalType.BLOOD_PRESSURE);
		reading.setSystolic(120);
		reading.setDiastolic(null);

		assertThrows(IllegalArgumentException.class, () -> vitalsService.submitReading(reading));
		verify(vitalReadingRepository, never()).save(any());
	}

	@Test
	void submitReading_NonBloodPressure_MissingValue_ThrowsException() {
		VitalReading reading = new VitalReading();
		reading.setPatientId(1L);
		reading.setVitalType(VitalType.HEART_RATE);
		reading.setValue(null);

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> vitalsService.submitReading(reading));

		assertEquals("A reading value is required", ex.getMessage());
		verify(vitalReadingRepository, never()).save(any());
	}

	@Test
	void submitReading_AlertFails_StillReturnsReading() {
		VitalReading reading = buildHeartRateReading();
		reading.setValue(115.0);

		when(thresholdService.isAbnormal(reading)).thenReturn(true);
		when(thresholdService.formatReadingValue(reading)).thenReturn("115.0 BPM");
		when(thresholdService.getSafeRange(VitalType.HEART_RATE)).thenReturn("60-100 BPM");
		when(vitalReadingRepository.save(reading)).thenReturn(reading);
		when(patientServiceClient.getPatient(1L)).thenThrow(new RuntimeException("Patient service unavailable"));

		VitalReading result = vitalsService.submitReading(reading);

		assertNotNull(result);
		assertTrue(result.getAlertTriggered());
	}

	// ─── getReadingsByPatient ────────────────────────────────────────────────

	@Test
	void getReadingsByPatient_ReturnsList() {
		VitalReading r1 = buildBloodPressureReading();
		VitalReading r2 = buildHeartRateReading();

		when(vitalReadingRepository.findByPatientId(1L)).thenReturn(List.of(r1, r2));

		List<VitalReading> result = vitalsService.getReadingsByPatient(1L);

		assertEquals(2, result.size());
		verify(vitalReadingRepository, times(1)).findByPatientId(1L);
	}

	@Test
	void getReadingsByPatient_NoReadings_ReturnsEmpty() {
		when(vitalReadingRepository.findByPatientId(99L)).thenReturn(List.of());

		List<VitalReading> result = vitalsService.getReadingsByPatient(99L);

		assertTrue(result.isEmpty());
	}

	// ─── getReadingsByPatientAndType ─────────────────────────────────────────

	@Test
	void getReadingsByPatientAndType_FiltersByType() {
		VitalReading reading = buildBloodPressureReading();

		when(vitalReadingRepository.findByPatientIdAndVitalType(1L, VitalType.BLOOD_PRESSURE))
				.thenReturn(List.of(reading));

		List<VitalReading> result = vitalsService.getReadingsByPatientAndType(1L, VitalType.BLOOD_PRESSURE);

		assertEquals(1, result.size());
		assertEquals(VitalType.BLOOD_PRESSURE, result.get(0).getVitalType());
	}

	@Test
	void getReadingsByPatientAndType_NoMatch_ReturnsEmpty() {
		when(vitalReadingRepository.findByPatientIdAndVitalType(1L, VitalType.SPO2)).thenReturn(List.of());

		List<VitalReading> result = vitalsService.getReadingsByPatientAndType(1L, VitalType.SPO2);

		assertTrue(result.isEmpty());
	}
}