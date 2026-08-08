package com.meditracker.notificationservice.controller;

import com.meditracker.notificationservice.dto.VitalAlertEvent;
import com.meditracker.notificationservice.model.Notification;
import com.meditracker.notificationservice.model.NotificationStatus;
import com.meditracker.notificationservice.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	@Autowired
	private NotificationService notificationService;

	// GET /api/notifications?doctorId=1
	@GetMapping
	public ResponseEntity<List<Notification>> getNotifications(@RequestParam Long doctorId) {
		return ResponseEntity.ok(notificationService.getNotificationsByDoctorId(doctorId));
	}

	// GET /api/notifications/unread?doctorId=1
	@GetMapping("/unread")
	public ResponseEntity<List<Notification>> getUnreadNotifications(@RequestParam Long doctorId) {
		return ResponseEntity.ok(notificationService.getUnreadNotifications(doctorId));
	}

	// PATCH /api/notifications/{id}/read
	@PatchMapping("/{id}/read")
	public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
		return ResponseEntity.ok(notificationService.markAsRead(id));
	}

	// POST /api/notifications/alert — called directly by vitals-service
	@PostMapping("/alert")
	public ResponseEntity<Notification> createAlert(@RequestBody VitalAlertEvent event) {

		Notification notification = new Notification();
		notification.setDoctorId(event.getDoctorId());
		notification.setPatientId(event.getPatientId());
		notification.setPatientName(event.getPatientName());
		notification.setVitalType(event.getVitalType());
		notification.setReadingValue(event.getReadingValue());
		notification.setSafeRange(event.getSafeRange());
		notification.setTimestamp(event.getTimestamp());
		notification.setStatus(NotificationStatus.UNREAD);
		notification.setMessage("Patient " + event.getPatientName() + " submitted a "
				+ event.getVitalType().replace("_", " ").toLowerCase() + " reading of " + event.getReadingValue()
				+ " which is outside the safe range of " + event.getSafeRange() + ".");

		System.out.println("Alert received for patient: " + event.getPatientName());
		return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.saveNotification(notification));
	}
}