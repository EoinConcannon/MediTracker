package com.meditracker.notificationservice.kafka;

import com.meditracker.notificationservice.dto.VitalAlertEvent;
import com.meditracker.notificationservice.model.Notification;
import com.meditracker.notificationservice.model.NotificationStatus;
import com.meditracker.notificationservice.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class VitalAlertConsumer {

	@Autowired
	private NotificationRepository notificationRepository;

	@KafkaListener(topics = "vitals-alert", groupId = "notification-group")
	public void handleVitalAlert(VitalAlertEvent event) {
		System.out.println("Kafka event received for patient: " + event.getPatientName());

		Notification notification = new Notification();
		notification.setDoctorId(event.getDoctorId());
		notification.setPatientId(event.getPatientId());
		notification.setPatientName(event.getPatientName());
		notification.setVitalType(event.getVitalType());
		notification.setReadingValue(event.getReadingValue());
		notification.setSafeRange(event.getSafeRange());
		notification.setTimestamp(event.getTimestamp());
		notification.setStatus(NotificationStatus.UNREAD);
		notification.setMessage(buildMessage(event));

		notificationRepository.save(notification);
		System.out.println("Notification saved for doctor ID: " + event.getDoctorId());
	}

	private String buildMessage(VitalAlertEvent event) {
		return String.format("Patient %s submitted a %s reading of %s which is outside the safe range of %s.",
				event.getPatientName(), event.getVitalType().replace("_", " ").toLowerCase(), event.getReadingValue(),
				event.getSafeRange());
	}
}