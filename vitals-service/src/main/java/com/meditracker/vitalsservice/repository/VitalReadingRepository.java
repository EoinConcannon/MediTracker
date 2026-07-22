package com.meditracker.vitalsservice.repository;

import com.meditracker.vitalsservice.model.VitalReading;
import com.meditracker.vitalsservice.model.VitalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VitalReadingRepository extends JpaRepository<VitalReading, Long> {
	List<VitalReading> findByPatientId(Long patientId);

	List<VitalReading> findByPatientIdAndVitalType(Long patientId, VitalType vitalType);
}