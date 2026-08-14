package com.meditracker.patientservice;

import com.meditracker.patientservice.model.Patient;
import com.meditracker.patientservice.model.Prescription;
import com.meditracker.patientservice.model.PrescriptionStatus;
import com.meditracker.patientservice.repository.PatientRepository;
import com.meditracker.patientservice.repository.PrescriptionRepository;
import com.meditracker.patientservice.service.PrescriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PrescriptionServiceTest {

	@Mock
	private PrescriptionRepository prescriptionRepository;

	@Mock
	private PatientRepository patientRepository;

	@InjectMocks
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
		prescription.setEndDate(null);
		prescription.setAllergyWarning(false);
		return prescription;
	}

	private Patient buildPatient() {
		Patient patient = new Patient();
		patient.setId(1L);
		patient.setName("Séan O'Brien");
		patient.setEmail("sean.obrien@email.ie");
		patient.setAllergies("Penicillin");
		patient.setAssignedDoctorId(1L);
		return patient;
	}

	private Patient buildPatientNoAllergies() {
		Patient patient = buildPatient();
		patient.setAllergies("None");
		return patient;
	}

	// ─── issuePrescription ───────────────────────────────────────────────────

	@Test
	void issuePrescription_Success_NoAllergyConflict() {
		Prescription prescription = buildPrescription();
		Patient patient = buildPatient();

		when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
		when(prescriptionRepository.save(prescription)).thenReturn(prescription);

		Prescription result = prescriptionService.issuePrescription(prescription);

		assertNotNull(result);
		assertEquals(PrescriptionStatus.ACTIVE, result.getStatus());
		assertFalse(result.isAllergyWarning());
		verify(prescriptionRepository, times(1)).save(prescription);
	}

	@Test
	void issuePrescription_AllergyConflict_SetsWarningFlag() {
		Prescription prescription = buildPrescription();
		prescription.setDrugName("Penicillin");

		Patient patient = buildPatient();

		when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
		when(prescriptionRepository.save(prescription)).thenReturn(prescription);

		Prescription result = prescriptionService.issuePrescription(prescription);

		assertTrue(result.isAllergyWarning());
		assertEquals(PrescriptionStatus.ACTIVE, result.getStatus());
	}

	@Test
	void issuePrescription_PatientHasNoAllergies_NoWarning() {
		Prescription prescription = buildPrescription();
		Patient patient = buildPatientNoAllergies();

		when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
		when(prescriptionRepository.save(prescription)).thenReturn(prescription);

		Prescription result = prescriptionService.issuePrescription(prescription);

		assertFalse(result.isAllergyWarning());
		assertEquals(PrescriptionStatus.ACTIVE, result.getStatus());
	}

	@Test
	void issuePrescription_EndDateBeforeStartDate_ThrowsException() {
		Prescription prescription = buildPrescription();
		prescription.setStartDate(LocalDate.now());
		prescription.setEndDate(LocalDate.now().minusDays(1));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> prescriptionService.issuePrescription(prescription));

		assertEquals("End date cannot be before start date", ex.getMessage());
		verify(prescriptionRepository, never()).save(any());
	}

	@Test
	void issuePrescription_EndDateSameAsStartDate_IsAllowed() {
		Prescription prescription = buildPrescription();
		prescription.setStartDate(LocalDate.now());
		prescription.setEndDate(LocalDate.now());

		Patient patient = buildPatientNoAllergies();

		when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
		when(prescriptionRepository.save(prescription)).thenReturn(prescription);

		Prescription result = prescriptionService.issuePrescription(prescription);

		assertNotNull(result);
		assertEquals(PrescriptionStatus.ACTIVE, result.getStatus());
	}

	@Test
	void issuePrescription_NoEndDate_IsAllowed() {
		Prescription prescription = buildPrescription();
		prescription.setEndDate(null);

		Patient patient = buildPatientNoAllergies();

		when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
		when(prescriptionRepository.save(prescription)).thenReturn(prescription);

		Prescription result = prescriptionService.issuePrescription(prescription);

		assertNotNull(result);
		assertNull(result.getEndDate());
	}

	@Test
	void issuePrescription_PatientNotFound_StillSaves() {
		Prescription prescription = buildPrescription();

		when(patientRepository.findById(1L)).thenReturn(Optional.empty());
		when(prescriptionRepository.save(prescription)).thenReturn(prescription);

		Prescription result = prescriptionService.issuePrescription(prescription);

		assertNotNull(result);
		assertFalse(result.isAllergyWarning());
		assertEquals(PrescriptionStatus.ACTIVE, result.getStatus());
	}

	// ─── getPrescriptionsByPatient ───────────────────────────────────────────

	@Test
	void getPrescriptionsByPatient_ReturnsList() {
		Prescription p1 = buildPrescription();
		Prescription p2 = buildPrescription();
		p2.setId(2L);
		p2.setDrugName("Aspirin");

		when(prescriptionRepository.findByPatientId(1L)).thenReturn(List.of(p1, p2));

		List<Prescription> result = prescriptionService.getPrescriptionsByPatient(1L);

		assertEquals(2, result.size());
		verify(prescriptionRepository, times(1)).findByPatientId(1L);
	}

	@Test
	void getPrescriptionsByPatient_NoPrescriptions_ReturnsEmpty() {
		when(prescriptionRepository.findByPatientId(99L)).thenReturn(List.of());

		List<Prescription> result = prescriptionService.getPrescriptionsByPatient(99L);

		assertTrue(result.isEmpty());
	}

	// ─── getActivePrescriptionsByPatient ────────────────────────────────────

	@Test
	void getActivePrescriptions_ReturnsOnlyActive() {
		Prescription active = buildPrescription();
		active.setStatus(PrescriptionStatus.ACTIVE);

		when(prescriptionRepository.findByPatientIdAndStatus(1L, PrescriptionStatus.ACTIVE))
				.thenReturn(List.of(active));

		List<Prescription> result = prescriptionService.getActivePrescriptionsByPatient(1L);

		assertEquals(1, result.size());
		assertEquals(PrescriptionStatus.ACTIVE, result.get(0).getStatus());
	}

	@Test
	void getActivePrescriptions_NoneActive_ReturnsEmpty() {
		when(prescriptionRepository.findByPatientIdAndStatus(1L, PrescriptionStatus.ACTIVE)).thenReturn(List.of());

		List<Prescription> result = prescriptionService.getActivePrescriptionsByPatient(1L);

		assertTrue(result.isEmpty());
	}

	// ─── getPrescriptionsByDoctor ────────────────────────────────────────────

	@Test
	void getPrescriptionsByDoctor_ReturnsList() {
		Prescription p1 = buildPrescription();

		when(prescriptionRepository.findByDoctorId(1L)).thenReturn(List.of(p1));

		List<Prescription> result = prescriptionService.getPrescriptionsByDoctor(1L);

		assertEquals(1, result.size());
		verify(prescriptionRepository, times(1)).findByDoctorId(1L);
	}

	@Test
	void getPrescriptionsByDoctor_NoPrescriptions_ReturnsEmpty() {
		when(prescriptionRepository.findByDoctorId(99L)).thenReturn(List.of());

		List<Prescription> result = prescriptionService.getPrescriptionsByDoctor(99L);

		assertTrue(result.isEmpty());
	}
}