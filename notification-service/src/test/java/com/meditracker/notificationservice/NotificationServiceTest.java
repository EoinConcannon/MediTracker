package com.meditracker.notificationservice;

import com.meditracker.notificationservice.exception.NotificationNotFoundException;
import com.meditracker.notificationservice.model.Notification;
import com.meditracker.notificationservice.model.NotificationStatus;
import com.meditracker.notificationservice.repository.NotificationRepository;
import com.meditracker.notificationservice.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

	@Mock
	private NotificationRepository notificationRepository;

	@InjectMocks
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
				+ "of 155/95 mmHg which is outside the safe range of " + "Systolic 90-139 mmHg, Diastolic 60-89 mmHg.");
		notification.setTimestamp(LocalDateTime.now());
		notification.setStatus(NotificationStatus.UNREAD);
		return notification;
	}

	// ─── getNotificationsByDoctorId ──────────────────────────────────────────

	@Test
	void getNotificationsByDoctorId_ReturnsList() {
		Notification n1 = buildNotification();
		Notification n2 = buildNotification();
		n2.setId(2L);

		when(notificationRepository.findByDoctorId(1L)).thenReturn(List.of(n1, n2));

		List<Notification> result = notificationService.getNotificationsByDoctorId(1L);

		assertEquals(2, result.size());
		verify(notificationRepository, times(1)).findByDoctorId(1L);
	}

	@Test
	void getNotificationsByDoctorId_NoNotifications_ReturnsEmpty() {
		when(notificationRepository.findByDoctorId(99L)).thenReturn(List.of());

		List<Notification> result = notificationService.getNotificationsByDoctorId(99L);

		assertTrue(result.isEmpty());
	}

	@Test
	void getNotificationsByDoctorId_ReturnsCorrectDoctorNotifications() {
		Notification notification = buildNotification();

		when(notificationRepository.findByDoctorId(1L)).thenReturn(List.of(notification));

		List<Notification> result = notificationService.getNotificationsByDoctorId(1L);

		assertEquals(1L, result.get(0).getDoctorId());
		assertEquals("Séan O'Brien", result.get(0).getPatientName());
	}

	// ─── getUnreadNotifications ──────────────────────────────────────────────

	@Test
	void getUnreadNotifications_ReturnsOnlyUnread() {
		Notification unread = buildNotification();
		unread.setStatus(NotificationStatus.UNREAD);

		when(notificationRepository.findByDoctorIdAndStatus(1L, NotificationStatus.UNREAD)).thenReturn(List.of(unread));

		List<Notification> result = notificationService.getUnreadNotifications(1L);

		assertEquals(1, result.size());
		assertEquals(NotificationStatus.UNREAD, result.get(0).getStatus());
		verify(notificationRepository, times(1)).findByDoctorIdAndStatus(1L, NotificationStatus.UNREAD);
	}

	@Test
	void getUnreadNotifications_AllRead_ReturnsEmpty() {
		when(notificationRepository.findByDoctorIdAndStatus(1L, NotificationStatus.UNREAD)).thenReturn(List.of());

		List<Notification> result = notificationService.getUnreadNotifications(1L);

		assertTrue(result.isEmpty());
	}

	@Test
	void getUnreadNotifications_MultipleUnread_ReturnsAll() {
		Notification n1 = buildNotification();
		Notification n2 = buildNotification();
		n2.setId(2L);
		n2.setVitalType("HEART_RATE");

		when(notificationRepository.findByDoctorIdAndStatus(1L, NotificationStatus.UNREAD)).thenReturn(List.of(n1, n2));

		List<Notification> result = notificationService.getUnreadNotifications(1L);

		assertEquals(2, result.size());
	}

	// ─── saveNotification ────────────────────────────────────────────────────

	@Test
	void saveNotification_SavesAndReturns() {
		Notification notification = buildNotification();

		when(notificationRepository.save(notification)).thenReturn(notification);

		Notification result = notificationService.saveNotification(notification);

		assertNotNull(result);
		assertEquals("Séan O'Brien", result.getPatientName());
		assertEquals(NotificationStatus.UNREAD, result.getStatus());
		verify(notificationRepository, times(1)).save(notification);
	}

	@Test
	void saveNotification_SetsCorrectFields() {
		Notification notification = buildNotification();

		when(notificationRepository.save(notification)).thenReturn(notification);

		Notification result = notificationService.saveNotification(notification);

		assertEquals("BLOOD_PRESSURE", result.getVitalType());
		assertEquals("155/95 mmHg", result.getReadingValue());
		assertEquals(1L, result.getDoctorId());
		assertEquals(1L, result.getPatientId());
	}

	// ─── markAsRead ──────────────────────────────────────────────────────────

	@Test
	void markAsRead_Success_SetsReadStatus() {
		Notification notification = buildNotification();
		notification.setStatus(NotificationStatus.UNREAD);

		when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
		when(notificationRepository.save(notification)).thenReturn(notification);

		Notification result = notificationService.markAsRead(1L);

		assertEquals(NotificationStatus.READ, result.getStatus());
		verify(notificationRepository, times(1)).save(notification);
	}

	@Test
	void markAsRead_NotFound_ThrowsNotificationNotFoundException() {
		when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

		NotificationNotFoundException ex = assertThrows(NotificationNotFoundException.class,
				() -> notificationService.markAsRead(999L));

		assertEquals("Notification not found with id: 999", ex.getMessage());
		verify(notificationRepository, never()).save(any());
	}

	@Test
	void markAsRead_AlreadyRead_StillSaves() {
		Notification notification = buildNotification();
		notification.setStatus(NotificationStatus.READ);

		when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
		when(notificationRepository.save(notification)).thenReturn(notification);

		Notification result = notificationService.markAsRead(1L);

		assertEquals(NotificationStatus.READ, result.getStatus());
		verify(notificationRepository, times(1)).save(notification);
	}
}