package com.meditracker.patientservice.service;

import com.meditracker.patientservice.exception.DuplicateEmailException;
import com.meditracker.patientservice.exception.InvalidCredentialsException;
import com.meditracker.patientservice.exception.ResourceNotFoundException;
import com.meditracker.patientservice.model.Doctor;
import com.meditracker.patientservice.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorService {

	@Autowired
	private DoctorRepository doctorRepository;

	public Doctor registerDoctor(Doctor doctor) {
		if (doctorRepository.findByEmail(doctor.getEmail()).isPresent()) {
			throw new DuplicateEmailException("A doctor with this email already exists");
		}
		return doctorRepository.save(doctor);
	}

	public Doctor loginDoctor(String email, String password) {
		Doctor doctor = doctorRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("No account found with that email address"));

		if (!doctor.getPassword().equals(password)) {
			throw new InvalidCredentialsException("Incorrect password");
		}

		return doctor;
	}

	public Doctor getDoctorById(Long id) {
		return doctorRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
	}

	public List<Doctor> getAllDoctors() {
		return doctorRepository.findAll();
	}
}