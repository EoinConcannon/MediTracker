package com.meditracker.vitalsservice;

import com.meditracker.vitalsservice.service.NotificationServiceClient;
import com.meditracker.vitalsservice.service.PatientServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class VitalsServiceApplicationTests {

	@MockitoBean
	private PatientServiceClient patientServiceClient;

	@MockitoBean
	private NotificationServiceClient notificationServiceClient;

	@Test
	void contextLoads() {
	}
}