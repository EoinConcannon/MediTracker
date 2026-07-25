package com.meditracker.patientservice.controller;

import com.meditracker.patientservice.dto.LoginRequest;
import com.meditracker.patientservice.model.Patient;
import com.meditracker.patientservice.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

	@Autowired
	private PatientService patientService;

	@PostMapping
	public ResponseEntity<Patient> registerPatient(@Valid @RequestBody Patient patient) {
		Patient saved = patientService.registerPatient(patient);
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Patient> getPatientById(@PathVariable Long id,
			@RequestParam(required = false) Long doctorId) {

		if (doctorId != null) {
			return ResponseEntity.ok(patientService.getPatientByIdForDoctor(id, doctorId));
		}

		return ResponseEntity.ok(patientService.getPatientById(id));
	}

	@GetMapping
	public ResponseEntity<List<Patient>> getPatients(@RequestParam(required = false) Long doctorId) {

		if (doctorId != null) {
			return ResponseEntity.ok(patientService.getPatientsByDoctorId(doctorId));
		}

		return ResponseEntity.ok(patientService.getAllPatients());
	}

	@PostMapping("/login")
	public ResponseEntity<Patient> loginPatient(@RequestBody LoginRequest loginRequest) {
		Patient patient = patientService.loginPatient(loginRequest.getEmail(), loginRequest.getPassword());
		return ResponseEntity.ok(patient);
	}
}