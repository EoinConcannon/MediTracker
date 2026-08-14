package com.meditracker.patientservice;

import com.meditracker.patientservice.exception.DuplicateEmailException;
import com.meditracker.patientservice.exception.InvalidCredentialsException;
import com.meditracker.patientservice.exception.ResourceNotFoundException;
import com.meditracker.patientservice.model.Doctor;
import com.meditracker.patientservice.repository.DoctorRepository;
import com.meditracker.patientservice.service.DoctorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DoctorServiceTest {

	@Mock
	private DoctorRepository doctorRepository;

	@InjectMocks
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

	// ─── registerDoctor ──────────────────────────────────────────────────────

	@Test
	void registerDoctor_Success() {
		Doctor doctor = buildDoctor();

		when(doctorRepository.findByEmail(doctor.getEmail())).thenReturn(Optional.empty());
		when(doctorRepository.save(doctor)).thenReturn(doctor);

		Doctor result = doctorService.registerDoctor(doctor);

		assertNotNull(result);
		assertEquals("Dr. Sarah Murphy", result.getName());
		verify(doctorRepository, times(1)).save(doctor);
	}

	@Test
	void registerDoctor_DuplicateEmail_ThrowsException() {
		Doctor doctor = buildDoctor();

		when(doctorRepository.findByEmail(doctor.getEmail())).thenReturn(Optional.of(doctor));

		assertThrows(DuplicateEmailException.class, () -> doctorService.registerDoctor(doctor));
		verify(doctorRepository, never()).save(any());
	}

	// ─── loginDoctor ─────────────────────────────────────────────────────────

	@Test
	void loginDoctor_Success() {
		Doctor doctor = buildDoctor();

		when(doctorRepository.findByEmail("sarah.murphy@meditracker.com")).thenReturn(Optional.of(doctor));

		Doctor result = doctorService.loginDoctor("sarah.murphy@meditracker.com", "password123");

		assertNotNull(result);
		assertEquals("Dr. Sarah Murphy", result.getName());
	}

	@Test
	void loginDoctor_EmailNotFound_ThrowsResourceNotFound() {
		when(doctorRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class,
				() -> doctorService.loginDoctor("nobody@test.com", "password123"));
	}

	@Test
	void loginDoctor_WrongPassword_ThrowsInvalidCredentials() {
		Doctor doctor = buildDoctor();

		when(doctorRepository.findByEmail("sarah.murphy@meditracker.com")).thenReturn(Optional.of(doctor));

		assertThrows(InvalidCredentialsException.class,
				() -> doctorService.loginDoctor("sarah.murphy@meditracker.com", "wrongpassword"));
	}

	// ─── getDoctorById ───────────────────────────────────────────────────────

	@Test
	void getDoctorById_Success() {
		Doctor doctor = buildDoctor();

		when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

		Doctor result = doctorService.getDoctorById(1L);

		assertNotNull(result);
		assertEquals(1L, result.getId());
	}

	@Test
	void getDoctorById_NotFound_ThrowsResourceNotFound() {
		when(doctorRepository.findById(999L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> doctorService.getDoctorById(999L));
	}

	// ─── getAllDoctors ───────────────────────────────────────────────────────

	@Test
	void getAllDoctors_ReturnsList() {
		Doctor d1 = buildDoctor();
		Doctor d2 = buildDoctor();
		d2.setId(2L);
		d2.setEmail("other@meditracker.com");

		when(doctorRepository.findAll()).thenReturn(List.of(d1, d2));

		List<Doctor> result = doctorService.getAllDoctors();

		assertEquals(2, result.size());
		verify(doctorRepository, times(1)).findAll();
	}

	@Test
	void getAllDoctors_EmptyList() {
		when(doctorRepository.findAll()).thenReturn(List.of());

		List<Doctor> result = doctorService.getAllDoctors();

		assertTrue(result.isEmpty());
	}
}