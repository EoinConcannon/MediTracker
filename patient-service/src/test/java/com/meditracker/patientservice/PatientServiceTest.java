package com.meditracker.patientservice;

import com.meditracker.patientservice.exception.DuplicateEmailException;
import com.meditracker.patientservice.exception.InvalidCredentialsException;
import com.meditracker.patientservice.exception.ResourceNotFoundException;
import com.meditracker.patientservice.exception.UnauthorisedAccessException;
import com.meditracker.patientservice.model.Doctor;
import com.meditracker.patientservice.model.Patient;
import com.meditracker.patientservice.repository.DoctorRepository;
import com.meditracker.patientservice.repository.PatientRepository;
import com.meditracker.patientservice.service.PatientService;
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
public class PatientServiceTest {

	@Mock
	private PatientRepository patientRepository;

	@Mock
	private DoctorRepository doctorRepository;

	@InjectMocks
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

	private Doctor buildDoctor() {
		Doctor doctor = new Doctor();
		doctor.setId(1L);
		doctor.setName("Dr. Sarah Murphy");
		doctor.setEmail("sarah.murphy@meditracker.com");
		doctor.setPassword("password123");
		return doctor;
	}

	// ─── registerPatient ─────────────────────────────────────────────────────

	@Test
	void registerPatient_Success() {
		Patient patient = buildPatient();
		Doctor doctor = buildDoctor();

		when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
		when(patientRepository.findByEmail(patient.getEmail())).thenReturn(Optional.empty());
		when(patientRepository.save(patient)).thenReturn(patient);

		Patient result = patientService.registerPatient(patient);

		assertNotNull(result);
		assertEquals("Séan O'Brien", result.getName());
		verify(patientRepository, times(1)).save(patient);
	}

	@Test
	void registerPatient_DoctorNotFound_ThrowsResourceNotFound() {
		Patient patient = buildPatient();

		when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> patientService.registerPatient(patient));
		verify(patientRepository, never()).save(any());
	}

	@Test
	void registerPatient_DuplicateEmail_ThrowsException() {
		Patient patient = buildPatient();
		Doctor doctor = buildDoctor();

		when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
		when(patientRepository.findByEmail(patient.getEmail())).thenReturn(Optional.of(patient));

		assertThrows(DuplicateEmailException.class, () -> patientService.registerPatient(patient));
		verify(patientRepository, never()).save(any());
	}

	// ─── loginPatient ────────────────────────────────────────────────────────

	@Test
	void loginPatient_Success() {
		Patient patient = buildPatient();

		when(patientRepository.findByEmail("sean.obrien@email.ie")).thenReturn(Optional.of(patient));

		Patient result = patientService.loginPatient("sean.obrien@email.ie", "password123");

		assertNotNull(result);
		assertEquals("Séan O'Brien", result.getName());
	}

	@Test
	void loginPatient_EmailNotFound_ThrowsResourceNotFound() {
		when(patientRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> patientService.loginPatient("nobody@test.com", "password123"));
	}

	@Test
	void loginPatient_WrongPassword_ThrowsInvalidCredentials() {
		Patient patient = buildPatient();

		when(patientRepository.findByEmail("sean.obrien@email.ie")).thenReturn(Optional.of(patient));

		assertThrows(InvalidCredentialsException.class,
				() -> patientService.loginPatient("sean.obrien@email.ie", "wrongpassword"));
	}

	// ─── getPatientById ──────────────────────────────────────────────────────

	@Test
	void getPatientById_Success() {
		Patient patient = buildPatient();

		when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

		Patient result = patientService.getPatientById(1L);

		assertNotNull(result);
		assertEquals(1L, result.getId());
	}

	@Test
	void getPatientById_NotFound_ThrowsResourceNotFound() {
		when(patientRepository.findById(999L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> patientService.getPatientById(999L));
	}

	// ─── getPatientByIdForDoctor ─────────────────────────────────────────────

	@Test
	void getPatientByIdForDoctor_Success() {
		Patient patient = buildPatient();

		when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

		Patient result = patientService.getPatientByIdForDoctor(1L, 1L);

		assertNotNull(result);
		assertEquals("Séan O'Brien", result.getName());
	}

	@Test
	void getPatientByIdForDoctor_WrongDoctor_ThrowsUnauthorised() {
		Patient patient = buildPatient();

		when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

		assertThrows(UnauthorisedAccessException.class, () -> patientService.getPatientByIdForDoctor(1L, 99L));
	}

	@Test
	void getPatientByIdForDoctor_PatientNotFound_ThrowsResourceNotFound() {
		when(patientRepository.findById(999L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> patientService.getPatientByIdForDoctor(999L, 1L));
	}

	// ─── getPatientsByDoctorId ───────────────────────────────────────────────

	@Test
	void getPatientsByDoctorId_ReturnsList() {
		Patient p1 = buildPatient();
		Patient p2 = buildPatient();
		p2.setId(2L);
		p2.setEmail("aoife.kelly@email.ie");

		when(patientRepository.findByAssignedDoctorId(1L)).thenReturn(List.of(p1, p2));

		List<Patient> result = patientService.getPatientsByDoctorId(1L);

		assertEquals(2, result.size());
		verify(patientRepository, times(1)).findByAssignedDoctorId(1L);
	}

	@Test
	void getPatientsByDoctorId_NoPatients_ReturnsEmpty() {
		when(patientRepository.findByAssignedDoctorId(99L)).thenReturn(List.of());

		List<Patient> result = patientService.getPatientsByDoctorId(99L);

		assertTrue(result.isEmpty());
	}

	// ─── getAllPatients ──────────────────────────────────────────────────────

	@Test
	void getAllPatients_ReturnsList() {
		Patient p1 = buildPatient();
		Patient p2 = buildPatient();
		p2.setId(2L);

		when(patientRepository.findAll()).thenReturn(List.of(p1, p2));

		List<Patient> result = patientService.getAllPatients();

		assertEquals(2, result.size());
		verify(patientRepository, times(1)).findAll();
	}
}