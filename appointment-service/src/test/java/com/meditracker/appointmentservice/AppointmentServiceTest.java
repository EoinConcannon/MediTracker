package com.meditracker.appointmentservice;

import com.meditracker.appointmentservice.exception.AppointmentConflictException;
import com.meditracker.appointmentservice.exception.ResourceNotFoundException;
import com.meditracker.appointmentservice.model.Appointment;
import com.meditracker.appointmentservice.model.AppointmentStatus;
import com.meditracker.appointmentservice.repository.AppointmentRepository;
import com.meditracker.appointmentservice.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {

	@Mock
	private AppointmentRepository appointmentRepository;

	@InjectMocks
	private AppointmentService appointmentService;

	// Helper to build a valid future appointment
	private Appointment buildAppointment() {
		Appointment appointment = new Appointment();
		appointment.setPatientId(1L);
		appointment.setDoctorId(1L);
		appointment.setDate(LocalDate.now().plusDays(7));
		appointment.setTime(LocalTime.of(10, 0));
		appointment.setNotes("General check-up");
		return appointment;
	}

	// ─── bookAppointment ────────────────────────────────────────────────────

	@Test
	void bookAppointment_Success() {
		Appointment appointment = buildAppointment();

		when(appointmentRepository.findByDoctorIdAndDateAndTimeAndStatusNot(anyLong(), any(), any(), any()))
				.thenReturn(Optional.empty());

		when(appointmentRepository.findByPatientIdAndDateAndTimeAndStatusNot(anyLong(), any(), any(), any()))
				.thenReturn(Optional.empty());

		when(appointmentRepository.save(appointment)).thenReturn(appointment);

		Appointment result = appointmentService.bookAppointment(appointment);

		assertNotNull(result);
		assertEquals(AppointmentStatus.SCHEDULED, result.getStatus());
		verify(appointmentRepository, times(1)).save(appointment);
	}

	@Test
	void bookAppointment_PastDate_ThrowsIllegalArgument() {
		Appointment appointment = buildAppointment();
		appointment.setDate(LocalDate.now().minusDays(1));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> appointmentService.bookAppointment(appointment));

		assertEquals("Appointment date cannot be in the past", ex.getMessage());
		verify(appointmentRepository, never()).save(any());
	}

	@Test
	void bookAppointment_DoctorAlreadyBooked_ThrowsConflict() {
		Appointment appointment = buildAppointment();
		Appointment existing = buildAppointment();

		when(appointmentRepository.findByDoctorIdAndDateAndTimeAndStatusNot(anyLong(), any(), any(), any()))
				.thenReturn(Optional.of(existing));

		AppointmentConflictException ex = assertThrows(AppointmentConflictException.class,
				() -> appointmentService.bookAppointment(appointment));

		assertEquals("Doctor already has an appointment at this date and time", ex.getMessage());
		verify(appointmentRepository, never()).save(any());
	}

	@Test
	void bookAppointment_PatientAlreadyBooked_ThrowsConflict() {
		Appointment appointment = buildAppointment();
		Appointment existing = buildAppointment();

		when(appointmentRepository.findByDoctorIdAndDateAndTimeAndStatusNot(anyLong(), any(), any(), any()))
				.thenReturn(Optional.empty());

		when(appointmentRepository.findByPatientIdAndDateAndTimeAndStatusNot(anyLong(), any(), any(), any()))
				.thenReturn(Optional.of(existing));

		AppointmentConflictException ex = assertThrows(AppointmentConflictException.class,
				() -> appointmentService.bookAppointment(appointment));

		assertEquals("You already have an appointment at this date and time", ex.getMessage());
		verify(appointmentRepository, never()).save(any());
	}

	@Test
	void bookAppointment_TodayDate_IsAllowed() {
		Appointment appointment = buildAppointment();
		appointment.setDate(LocalDate.now());

		when(appointmentRepository.findByDoctorIdAndDateAndTimeAndStatusNot(anyLong(), any(), any(), any()))
				.thenReturn(Optional.empty());

		when(appointmentRepository.findByPatientIdAndDateAndTimeAndStatusNot(anyLong(), any(), any(), any()))
				.thenReturn(Optional.empty());

		when(appointmentRepository.save(appointment)).thenReturn(appointment);

		Appointment result = appointmentService.bookAppointment(appointment);

		assertNotNull(result);
		assertEquals(AppointmentStatus.SCHEDULED, result.getStatus());
	}

	// ─── cancelAppointment ──────────────────────────────────────────────────

	@Test
	void cancelAppointment_Success() {
		Appointment appointment = buildAppointment();
		appointment.setId(1L);
		appointment.setStatus(AppointmentStatus.SCHEDULED);

		when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

		when(appointmentRepository.save(appointment)).thenReturn(appointment);

		Appointment result = appointmentService.cancelAppointment(1L);

		assertEquals(AppointmentStatus.CANCELLED, result.getStatus());
		verify(appointmentRepository, times(1)).save(appointment);
	}

	@Test
	void cancelAppointment_NotFound_ThrowsResourceNotFound() {
		when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

		ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
				() -> appointmentService.cancelAppointment(999L));

		assertEquals("Appointment not found with id: 999", ex.getMessage());
	}

	@Test
	void cancelAppointment_AlreadyCompleted_ThrowsIllegalArgument() {
		Appointment appointment = buildAppointment();
		appointment.setId(1L);
		appointment.setStatus(AppointmentStatus.COMPLETED);

		when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> appointmentService.cancelAppointment(1L));

		assertEquals("Cannot cancel a completed appointment", ex.getMessage());
		verify(appointmentRepository, never()).save(any());
	}

	@Test
	void cancelAppointment_AlreadyCancelled_ThrowsIllegalArgument() {
		Appointment appointment = buildAppointment();
		appointment.setId(1L);
		appointment.setStatus(AppointmentStatus.CANCELLED);

		when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> appointmentService.cancelAppointment(1L));

		assertEquals("Appointment is already cancelled", ex.getMessage());
		verify(appointmentRepository, never()).save(any());
	}

	// ─── getAppointmentsByPatient ────────────────────────────────────────────

	@Test
	void getAppointmentsByPatient_ReturnsCorrectList() {
		Appointment a1 = buildAppointment();
		Appointment a2 = buildAppointment();
		a2.setDate(LocalDate.now().plusDays(14));

		when(appointmentRepository.findByPatientId(1L)).thenReturn(List.of(a1, a2));

		List<Appointment> result = appointmentService.getAppointmentsByPatient(1L);

		assertEquals(2, result.size());
		verify(appointmentRepository, times(1)).findByPatientId(1L);
	}

	@Test
	void getAppointmentsByPatient_NoAppointments_ReturnsEmptyList() {
		when(appointmentRepository.findByPatientId(99L)).thenReturn(List.of());

		List<Appointment> result = appointmentService.getAppointmentsByPatient(99L);

		assertTrue(result.isEmpty());
	}

	// ─── getAppointmentsByDoctor ─────────────────────────────────────────────

	@Test
	void getAppointmentsByDoctor_ReturnsCorrectList() {
		Appointment a1 = buildAppointment();

		when(appointmentRepository.findByDoctorId(1L)).thenReturn(List.of(a1));

		List<Appointment> result = appointmentService.getAppointmentsByDoctor(1L);

		assertEquals(1, result.size());
		verify(appointmentRepository, times(1)).findByDoctorId(1L);
	}

	// ─── getUpcomingAppointmentsByPatient ────────────────────────────────────

	@Test
	void getUpcomingAppointmentsByPatient_FiltersOutPastDates() {
		Appointment future = buildAppointment();
		future.setDate(LocalDate.now().plusDays(5));
		future.setStatus(AppointmentStatus.SCHEDULED);

		Appointment past = buildAppointment();
		past.setDate(LocalDate.now().minusDays(5));
		past.setStatus(AppointmentStatus.SCHEDULED);

		when(appointmentRepository.findByPatientIdAndStatus(1L, AppointmentStatus.SCHEDULED))
				.thenReturn(List.of(future, past));

		List<Appointment> result = appointmentService.getUpcomingAppointmentsByPatient(1L);

		assertEquals(1, result.size());
		assertEquals(future.getDate(), result.get(0).getDate());
	}

	@Test
	void getUpcomingAppointmentsByPatient_NoUpcoming_ReturnsEmpty() {
		Appointment past = buildAppointment();
		past.setDate(LocalDate.now().minusDays(3));
		past.setStatus(AppointmentStatus.SCHEDULED);

		when(appointmentRepository.findByPatientIdAndStatus(1L, AppointmentStatus.SCHEDULED)).thenReturn(List.of(past));

		List<Appointment> result = appointmentService.getUpcomingAppointmentsByPatient(1L);

		assertTrue(result.isEmpty());
	}
}