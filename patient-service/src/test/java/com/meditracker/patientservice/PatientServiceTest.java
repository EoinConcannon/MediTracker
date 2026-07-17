package com.meditracker.patientservice;

import com.meditracker.patientservice.exception.DuplicateEmailException;
import com.meditracker.patientservice.exception.ResourceNotFoundException;
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
		patient.setName("John Murphy");
		patient.setEmail("john@test.com");
		patient.setDateOfBirth(LocalDate.of(1985, 3, 12));
		patient.setAssignedDoctorId(1L);
		return patient;
	}

	@Test
	void registerPatient_Success() {
		Patient patient = buildPatient();
		Doctor doctor = new Doctor("Dr. Sarah Murphy", "sarah@test.com", "Cardiology", "password");

		when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
		when(patientRepository.findByEmail("john@test.com")).thenReturn(Optional.empty());
		when(patientRepository.save(patient)).thenReturn(patient);

		Patient result = patientService.registerPatient(patient);

		assertNotNull(result);
		assertEquals("John Murphy", result.getName());
		verify(patientRepository, times(1)).save(patient);
	}

	@Test
	void registerPatient_DuplicateEmail_ThrowsException() {
		Patient patient = buildPatient();
		Doctor doctor = new Doctor("Dr. Sarah Murphy", "sarah@test.com", "Cardiology", "password");

		when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
		when(patientRepository.findByEmail("john@test.com")).thenReturn(Optional.of(patient));

		assertThrows(DuplicateEmailException.class, () -> patientService.registerPatient(patient));
		verify(patientRepository, never()).save(any());
	}

	@Test
	void registerPatient_InvalidDoctorId_ThrowsException() {
		Patient patient = buildPatient();

		when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> patientService.registerPatient(patient));
		verify(patientRepository, never()).save(any());
	}

	@Test
	void getPatientById_NotFound_ThrowsException() {
		when(patientRepository.findById(999L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> patientService.getPatientById(999L));
	}
}