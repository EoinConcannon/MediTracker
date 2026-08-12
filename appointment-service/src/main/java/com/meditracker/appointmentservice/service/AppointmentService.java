package com.meditracker.appointmentservice.service;

import com.meditracker.appointmentservice.exception.AppointmentConflictException;
import com.meditracker.appointmentservice.exception.ResourceNotFoundException;
import com.meditracker.appointmentservice.model.Appointment;
import com.meditracker.appointmentservice.model.AppointmentStatus;
import com.meditracker.appointmentservice.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AppointmentService {

	@Autowired
	private AppointmentRepository appointmentRepository;

	public Appointment bookAppointment(Appointment appointment) {

		// Reject past dates
		if (appointment.getDate().isBefore(LocalDate.now())) {
			throw new IllegalArgumentException("Appointment date cannot be in the past");
		}

		// Check doctor is not already booked at that time
		appointmentRepository.findByDoctorIdAndDateAndTimeAndStatusNot(appointment.getDoctorId(), appointment.getDate(),
				appointment.getTime(), AppointmentStatus.CANCELLED).ifPresent(existing -> {
					throw new AppointmentConflictException("Doctor already has an appointment at this date and time");
				});

		// Check patient does not already have an appointment at that time
		appointmentRepository.findByPatientIdAndDateAndTimeAndStatusNot(appointment.getPatientId(),
				appointment.getDate(), appointment.getTime(), AppointmentStatus.CANCELLED).ifPresent(existing -> {
					throw new AppointmentConflictException("You already have an appointment at this date and time");
				});

		appointment.setStatus(AppointmentStatus.PENDING);
		return appointmentRepository.save(appointment);
	}

	public List<Appointment> getAppointmentsByPatient(Long patientId) {
		return appointmentRepository.findByPatientId(patientId);
	}

	public List<Appointment> getAppointmentsByDoctor(Long doctorId) {
		return appointmentRepository.findByDoctorId(doctorId);
	}

	public List<Appointment> getUpcomingAppointmentsByPatient(Long patientId) {
		return appointmentRepository.findByPatientIdAndStatus(patientId, AppointmentStatus.SCHEDULED).stream()
				.filter(a -> !a.getDate().isBefore(LocalDate.now())).toList();
	}

	public List<Appointment> getPendingAppointmentsByDoctor(Long doctorId) {
		return appointmentRepository.findByDoctorIdAndStatus(doctorId, AppointmentStatus.PENDING);
	}

	public Appointment confirmAppointment(Long id) {
		Appointment appointment = appointmentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

		if (appointment.getStatus() != AppointmentStatus.PENDING) {
			throw new IllegalArgumentException("Only pending appointments can be confirmed");
		}

		appointment.setStatus(AppointmentStatus.SCHEDULED);
		return appointmentRepository.save(appointment);
	}

	public Appointment cancelAppointment(Long id) {
		Appointment appointment = appointmentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

		if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
			throw new IllegalArgumentException("Cannot cancel a completed appointment");
		}

		if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
			throw new IllegalArgumentException("Appointment is already cancelled");
		}

		appointment.setStatus(AppointmentStatus.CANCELLED);
		return appointmentRepository.save(appointment);
	}
}