package com.meditracker.patientservice.controller;

import com.meditracker.patientservice.model.Prescription;
import com.meditracker.patientservice.service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

	@Autowired
	private PrescriptionService prescriptionService;

	@PostMapping
	public ResponseEntity<Prescription> issuePrescription(@Valid @RequestBody Prescription prescription) {
		return ResponseEntity.status(HttpStatus.CREATED).body(prescriptionService.issuePrescription(prescription));
	}

	@GetMapping
	public ResponseEntity<List<Prescription>> getPrescriptions(@RequestParam(required = false) Long patientId,
			@RequestParam(required = false) Long doctorId, @RequestParam(required = false) String status) {

		if (patientId != null && "ACTIVE".equalsIgnoreCase(status)) {
			return ResponseEntity.ok(prescriptionService.getActivePrescriptionsByPatient(patientId));
		}
		if (patientId != null) {
			return ResponseEntity.ok(prescriptionService.getPrescriptionsByPatient(patientId));
		}
		if (doctorId != null) {
			return ResponseEntity.ok(prescriptionService.getPrescriptionsByDoctor(doctorId));
		}
		return ResponseEntity.badRequest().build();
	}
}