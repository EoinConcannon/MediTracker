package com.meditracker.patientservice.service;

import com.meditracker.patientservice.exception.DuplicateEmailException;
import com.meditracker.patientservice.exception.ResourceNotFoundException;
import com.meditracker.patientservice.model.Patient;
import com.meditracker.patientservice.repository.DoctorRepository;
import com.meditracker.patientservice.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private DoctorRepository doctorRepository;

	public Patient registerPatient(Patient patient) {
		// Check the assigned doctor actually exists
		doctorRepository.findById(patient.getAssignedDoctorId()).orElseThrow(
				() -> new ResourceNotFoundException("Doctor not found with id: " + patient.getAssignedDoctorId()));

		// Check for duplicate email
		if (patientRepository.findByEmail(patient.getEmail()).isPresent()) {
			throw new DuplicateEmailException("A patient with this email already exists");
		}

		return patientRepository.save(patient);
	}

	public Patient getPatientById(Long id) {
		return patientRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
	}

	public List<Patient> getPatientsByDoctorId(Long doctorId) {
		return patientRepository.findByAssignedDoctorId(doctorId);
	}
}