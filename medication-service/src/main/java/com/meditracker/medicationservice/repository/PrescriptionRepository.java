package com.meditracker.medicationservice.repository;

import com.meditracker.medicationservice.model.Prescription;
import com.meditracker.medicationservice.model.PrescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
	List<Prescription> findByPatientId(Long patientId);

	List<Prescription> findByPatientIdAndStatus(Long patientId, PrescriptionStatus status);

	List<Prescription> findByDoctorId(Long doctorId);
}