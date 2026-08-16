package com.meditracker.notificationservice;

import tools.jackson.databind.ObjectMapper;
import com.meditracker.notificationservice.dto.VitalAlertEvent;
import com.meditracker.notificationservice.exception.NotificationNotFoundException;
import com.meditracker.notificationservice.model.Notification;
import com.meditracker.notificationservice.model.NotificationStatus;
import com.meditracker.notificationservice.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class NotificationControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private NotificationService notificationService;

	private Notification buildNotification() {
		Notification notification = new Notification();
		notification.setId(1L);
		notification.setDoctorId(1L);
		notification.setPatientId(1L);
		notification.setPatientName("Séan O'Brien");
		notification.setVitalType("BLOOD_PRESSURE");
		notification.setReadingValue("155/95 mmHg");
		notification.setSafeRange("Systolic 90-139 mmHg, Diastolic 60-89 mmHg");
		notification.setMessage("Patient Séan O'Brien submitted a blood pressure reading "
				+ "of 155/95 mmHg which is outside the safe range.");
		notification.setTimestamp(LocalDateTime.now());
		notification.setStatus(NotificationStatus.UNREAD);
		return notification;
	}

	private VitalAlertEvent buildAlertEvent() {
		VitalAlertEvent event = new VitalAlertEvent();
		event.setPatientId(1L);
		event.setPatientName("Séan O'Brien");
		event.setDoctorId(1L);
		event.setVitalType("BLOOD_PRESSURE");
		event.setReadingValue("155/95 mmHg");
		event.setSafeRange("Systolic 90-139 mmHg, Diastolic 60-89 mmHg");
		event.setTimestamp(LocalDateTime.now());
		return event;
	}

	// ─── GET /api/notifications ──────────────────────────────────────────────

	@Test
	void getNotifications_ByDoctorId_Returns200() throws Exception {
		Notification notification = buildNotification();

		when(notificationService.getNotificationsByDoctorId(1L)).thenReturn(List.of(notification));

		mockMvc.perform(get("/api/notifications").param("doctorId", "1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].patientName").value("Séan O'Brien"))
				.andExpect(jsonPath("$[0].status").value("UNREAD"));
	}

	@Test
	void getNotifications_NoNotifications_Returns200Empty() throws Exception {
		when(notificationService.getNotificationsByDoctorId(99L)).thenReturn(List.of());

		mockMvc.perform(get("/api/notifications").param("doctorId", "99")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void getNotifications_MultipleNotifications_ReturnsAll() throws Exception {
		Notification n1 = buildNotification();
		Notification n2 = buildNotification();
		n2.setId(2L);
		n2.setVitalType("HEART_RATE");

		when(notificationService.getNotificationsByDoctorId(1L)).thenReturn(List.of(n1, n2));

		mockMvc.perform(get("/api/notifications").param("doctorId", "1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));
	}

	// ─── GET /api/notifications/unread ──────────────────────────────────────

	@Test
	void getUnreadNotifications_Returns200WithUnread() throws Exception {
		Notification notification = buildNotification();

		when(notificationService.getUnreadNotifications(1L)).thenReturn(List.of(notification));

		mockMvc.perform(get("/api/notifications/unread").param("doctorId", "1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].status").value("UNREAD"));
	}

	@Test
	void getUnreadNotifications_AllRead_Returns200Empty() throws Exception {
		when(notificationService.getUnreadNotifications(1L)).thenReturn(List.of());

		mockMvc.perform(get("/api/notifications/unread").param("doctorId", "1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	// ─── PATCH /api/notifications/{id}/read ─────────────────────────────────

	@Test
	void markAsRead_Success_Returns200() throws Exception {
		Notification notification = buildNotification();
		notification.setStatus(NotificationStatus.READ);

		when(notificationService.markAsRead(1L)).thenReturn(notification);

		mockMvc.perform(patch("/api/notifications/1/read")).andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("READ")).andExpect(jsonPath("$.id").value(1));
	}

	@Test
	void markAsRead_NotFound_Returns404() throws Exception {
		when(notificationService.markAsRead(999L)).thenThrow(new NotificationNotFoundException(999L));

		mockMvc.perform(patch("/api/notifications/999/read")).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("Notification not found with id: 999"));
	}

	// ─── POST /api/notifications/alert ──────────────────────────────────────

	@Test
	void createAlert_ValidEvent_Returns201() throws Exception {
		VitalAlertEvent event = buildAlertEvent();
		Notification notification = buildNotification();

		when(notificationService.saveNotification(any(Notification.class))).thenReturn(notification);

		mockMvc.perform(post("/api/notifications/alert").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(event))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.patientName").value("Séan O'Brien"))
				.andExpect(jsonPath("$.status").value("UNREAD"))
				.andExpect(jsonPath("$.vitalType").value("BLOOD_PRESSURE"));
	}

	@Test
	void createAlert_SetsCorrectMessage() throws Exception {
		VitalAlertEvent event = buildAlertEvent();
		Notification notification = buildNotification();
		notification.setMessage("Patient Séan O'Brien submitted a blood pressure reading "
				+ "of 155/95 mmHg which is outside the safe range of " + "Systolic 90-139 mmHg, Diastolic 60-89 mmHg.");

		when(notificationService.saveNotification(any(Notification.class))).thenReturn(notification);

		mockMvc.perform(post("/api/notifications/alert")
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(event)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.message").value("Patient Séan O'Brien submitted a blood pressure reading "
						+ "of 155/95 mmHg which is outside the safe range of "
						+ "Systolic 90-139 mmHg, Diastolic 60-89 mmHg."));
	}

	@Test
	void createAlert_DifferentVitalType_Returns201() throws Exception {
		VitalAlertEvent event = buildAlertEvent();
		event.setVitalType("HEART_RATE");
		event.setReadingValue("115.0 BPM");
		event.setSafeRange("60-100 BPM");

		Notification notification = buildNotification();
		notification.setVitalType("HEART_RATE");
		notification.setReadingValue("115.0 BPM");

		when(notificationService.saveNotification(any(Notification.class))).thenReturn(notification);

		mockMvc.perform(post("/api/notifications/alert").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(event))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.vitalType").value("HEART_RATE"));
	}

	@Test
	void createAlert_SetsUnreadStatus() throws Exception {
		VitalAlertEvent event = buildAlertEvent();
		Notification notification = buildNotification();
		notification.setStatus(NotificationStatus.UNREAD);

		when(notificationService.saveNotification(any(Notification.class))).thenReturn(notification);

		mockMvc.perform(post("/api/notifications/alert").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(event))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("UNREAD"));
	}
}