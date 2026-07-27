package com.meditracker.notificationservice.controller;

import com.meditracker.notificationservice.model.Notification;
import com.meditracker.notificationservice.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
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
}