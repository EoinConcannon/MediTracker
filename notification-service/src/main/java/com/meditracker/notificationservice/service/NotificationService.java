package com.meditracker.notificationservice.service;

import com.meditracker.notificationservice.model.Notification;
import com.meditracker.notificationservice.model.NotificationStatus;
import com.meditracker.notificationservice.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

	@Autowired
	private NotificationRepository notificationRepository;

	public List<Notification> getNotificationsByDoctorId(Long doctorId) {
		return notificationRepository.findByDoctorId(doctorId);
	}

	public List<Notification> getUnreadNotifications(Long doctorId) {
		return notificationRepository.findByDoctorIdAndStatus(doctorId, NotificationStatus.UNREAD);
	}

	public Notification saveNotification(Notification notification) {
		return notificationRepository.save(notification);
	}

	public Notification markAsRead(Long notificationId) {
		Notification notification = notificationRepository.findById(notificationId)
				.orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));
		notification.setStatus(NotificationStatus.READ);
		return notificationRepository.save(notification);
	}
}