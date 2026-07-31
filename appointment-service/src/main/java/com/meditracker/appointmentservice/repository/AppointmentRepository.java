package com.meditracker.appointmentservice.repository;

import com.meditracker.appointmentservice.model.Appointment;
import com.meditracker.appointmentservice.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

	List<Appointment> findByPatientId(Long patientId);

	List<Appointment> findByDoctorId(Long doctorId);

	List<Appointment> findByPatientIdAndStatus(Long patientId, AppointmentStatus status);

	// Check if doctor is already booked at that date and time
	Optional<Appointment> findByDoctorIdAndDateAndTimeAndStatusNot(Long doctorId, LocalDate date, LocalTime time,
			AppointmentStatus status);

	// Check if patient already has appointment at that date and time
	Optional<Appointment> findByPatientIdAndDateAndTimeAndStatusNot(Long patientId, LocalDate date, LocalTime time,
			AppointmentStatus status);
}