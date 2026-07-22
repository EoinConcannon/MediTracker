package com.meditracker.vitalsservice.controller;

import com.meditracker.vitalsservice.model.VitalReading;
import com.meditracker.vitalsservice.model.VitalType;
import com.meditracker.vitalsservice.service.VitalsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vitals")
public class VitalsController {

	@Autowired
	private VitalsService vitalsService;

	// POST /api/vitals — submit a vital reading
	@PostMapping
	public ResponseEntity<VitalReading> submitReading(@Valid @RequestBody VitalReading reading) {
		VitalReading saved = vitalsService.submitReading(reading);
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}

	// GET /api/vitals?patientId=1 — get all readings for a patient
	@GetMapping
	public ResponseEntity<List<VitalReading>> getReadings(@RequestParam Long patientId,
			@RequestParam(required = false) String vitalType) {

		if (vitalType != null) {
			return ResponseEntity
					.ok(vitalsService.getReadingsByPatientAndType(patientId, VitalType.valueOf(vitalType)));
		}

		return ResponseEntity.ok(vitalsService.getReadingsByPatient(patientId));
	}
}