package com.meditracker.vitalsservice.kafka;

import com.meditracker.vitalsservice.dto.VitalAlertEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class VitalAlertProducer {

	private static final String TOPIC = "vitals-alert";

	@Autowired
	private KafkaTemplate<String, VitalAlertEvent> kafkaTemplate;

	public void sendAlert(VitalAlertEvent event) {
		kafkaTemplate.send(TOPIC, event);
		System.out.println("Kafka alert sent for patient: " + event.getPatientName());
	}
}