package com.meditracker.vitalsservice;

import com.meditracker.vitalsservice.service.NotificationServiceClient;
import com.meditracker.vitalsservice.service.PatientServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
class VitalsServiceApplicationTests {

	@MockitoBean
	private PatientServiceClient patientServiceClient;

	@MockitoBean
	private NotificationServiceClient notificationServiceClient;

	@MockitoBean
	private RestTemplate restTemplate;

	@Test
	void contextLoads() {
	}
}