package com.meditracker.patientservice;

import com.meditracker.patientservice.exception.DuplicateEmailException;
import com.meditracker.patientservice.model.Doctor;
import com.meditracker.patientservice.repository.DoctorRepository;
import com.meditracker.patientservice.service.DoctorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DoctorServiceTest {

	@Mock
	private DoctorRepository doctorRepository;

	@InjectMocks
	private DoctorService doctorService;

	@Test
	void registerDoctor_Success() {
		Doctor doctor = new Doctor("Dr. Sarah Murphy", "sarah@test.com", "Cardiology", "password123");
		when(doctorRepository.findByEmail("sarah@test.com")).thenReturn(Optional.empty());
		when(doctorRepository.save(doctor)).thenReturn(doctor);

		Doctor result = doctorService.registerDoctor(doctor);

		assertNotNull(result);
		assertEquals("Dr. Sarah Murphy", result.getName());
		verify(doctorRepository, times(1)).save(doctor);
	}

	@Test
	void registerDoctor_DuplicateEmail_ThrowsException() {
		Doctor doctor = new Doctor("Dr. Sarah Murphy", "sarah@test.com", "Cardiology", "password123");
		when(doctorRepository.findByEmail("sarah@test.com")).thenReturn(Optional.of(doctor));

		assertThrows(DuplicateEmailException.class, () -> doctorService.registerDoctor(doctor));
		verify(doctorRepository, never()).save(any());
	}

	@Test
	void getDoctorById_NotFound_ThrowsException() {
		when(doctorRepository.findById(999L)).thenReturn(Optional.empty());

		assertThrows(RuntimeException.class, () -> doctorService.getDoctorById(999L));
	}
}