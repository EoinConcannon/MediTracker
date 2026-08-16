package com.meditracker.vitalsservice.service;

import com.meditracker.vitalsservice.dto.VitalAlertEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificationServiceClient {

	@Autowired
	private RestTemplate restTemplate;

	@Value("${notification.service.url:http://localhost:8085}")
	private String notificationServiceUrl;

	public void sendAlert(VitalAlertEvent event) {
		String url = notificationServiceUrl + "/api/notifications/alert";
		restTemplate.postForObject(url, event, Void.class);
	}
}