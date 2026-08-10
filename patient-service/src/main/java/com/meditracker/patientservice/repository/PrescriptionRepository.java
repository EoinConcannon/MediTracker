package com.meditracker.patientservice.repository;

import com.meditracker.patientservice.model.Prescription;
import com.meditracker.patientservice.model.PrescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
	List<Prescription> findByPatientId(Long patientId);

	List<Prescription> findByPatientIdAndStatus(Long patientId, PrescriptionStatus status);

	List<Prescription> findByDoctorId(Long doctorId);
}