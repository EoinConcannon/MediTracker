package com.meditracker.vitalsservice.service;

import com.meditracker.vitalsservice.dto.PatientDTO;
import com.meditracker.vitalsservice.dto.VitalAlertEvent;
import com.meditracker.vitalsservice.kafka.VitalAlertProducer;
import com.meditracker.vitalsservice.model.VitalReading;
import com.meditracker.vitalsservice.model.VitalType;
import com.meditracker.vitalsservice.repository.VitalReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VitalsService {

	@Autowired
	private VitalReadingRepository vitalReadingRepository;

	@Autowired
	private ThresholdService thresholdService;

	@Autowired
	private PatientServiceClient patientServiceClient;

	@Autowired
	private VitalAlertProducer vitalAlertProducer;

	public VitalReading submitReading(VitalReading reading) {

		// Validate blood pressure has both values
		if (reading.getVitalType() == VitalType.BLOOD_PRESSURE) {
			if (reading.getSystolic() == null || reading.getDiastolic() == null) {
				throw new IllegalArgumentException("Systolic and diastolic values are required for blood pressure");
			}
		} else {
			// All other types need a single value
			if (reading.getValue() == null) {
				throw new IllegalArgumentException("A reading value is required");
			}
		}

		// Check threshold
		boolean abnormal = thresholdService.isAbnormal(reading);
		reading.setAlertTriggered(abnormal);

		// Save the reading
		VitalReading saved = vitalReadingRepository.save(reading);

		// If abnormal, fetch patient details and publish Kafka event
		if (abnormal) {
			try {
				PatientDTO patient = patientServiceClient.getPatient(reading.getPatientId());

				VitalAlertEvent event = new VitalAlertEvent(patient.getId(), patient.getName(),
						patient.getAssignedDoctorId(), reading.getVitalType().name(),
						thresholdService.formatReadingValue(reading),
						thresholdService.getSafeRange(reading.getVitalType()), reading.getTimestamp());

				vitalAlertProducer.sendAlert(event);

			} catch (Exception e) {
				// Log the error but don't fail the request
				// The reading is already saved
				System.err.println("Failed to send Kafka alert: " + e.getMessage());
			}
		}

		return saved;
	}

	public List<VitalReading> getReadingsByPatient(Long patientId) {
		return vitalReadingRepository.findByPatientId(patientId);
	}

	public List<VitalReading> getReadingsByPatientAndType(Long patientId, VitalType vitalType) {
		return vitalReadingRepository.findByPatientIdAndVitalType(patientId, vitalType);
	}
}