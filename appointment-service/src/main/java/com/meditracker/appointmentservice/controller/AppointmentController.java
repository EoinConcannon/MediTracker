package com.meditracker.appointmentservice.controller;

import com.meditracker.appointmentservice.model.Appointment;
import com.meditracker.appointmentservice.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

	@Autowired
	private AppointmentService appointmentService;

	// POST /api/appointments — book an appointment
	@PostMapping
	public ResponseEntity<Appointment> bookAppointment(@Valid @RequestBody Appointment appointment) {
		return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.bookAppointment(appointment));
	}

	// GET /api/appointments?patientId=1
	@GetMapping
	public ResponseEntity<List<Appointment>> getAppointments(@RequestParam(required = false) Long patientId,
			@RequestParam(required = false) Long doctorId, @RequestParam(required = false) Boolean upcoming) {

		if (patientId != null && Boolean.TRUE.equals(upcoming)) {
			return ResponseEntity.ok(appointmentService.getUpcomingAppointmentsByPatient(patientId));
		}
		if (patientId != null) {
			return ResponseEntity.ok(appointmentService.getAppointmentsByPatient(patientId));
		}
		if (doctorId != null) {
			return ResponseEntity.ok(appointmentService.getAppointmentsByDoctor(doctorId));
		}
		return ResponseEntity.badRequest().build();
	}

	// PATCH /api/appointments/{id}/cancel
	@PatchMapping("/{id}/cancel")
	public ResponseEntity<Appointment> cancelAppointment(@PathVariable Long id) {
		return ResponseEntity.ok(appointmentService.cancelAppointment(id));
	}
}