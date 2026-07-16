package com.meditracker.patientservice.controller;

import com.meditracker.patientservice.model.Doctor;
import com.meditracker.patientservice.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

	@Autowired
	private DoctorService doctorService;

	// POST /api/doctors — register a new doctor
	@PostMapping
	public ResponseEntity<Doctor> registerDoctor(@Valid @RequestBody Doctor doctor) {
		Doctor saved = doctorService.registerDoctor(doctor);
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}

	// GET /api/doctors/{id} — get doctor by ID (used by React login)
	@GetMapping("/{id}")
	public ResponseEntity<Doctor> getDoctorById(@PathVariable Long id) {
		Doctor doctor = doctorService.getDoctorById(id);
		return ResponseEntity.ok(doctor);
	}

	// GET /api/doctors — get all doctors (useful for patient registration dropdown)
	@GetMapping
	public ResponseEntity<List<Doctor>> getAllDoctors() {
		return ResponseEntity.ok(doctorService.getAllDoctors());
	}
}