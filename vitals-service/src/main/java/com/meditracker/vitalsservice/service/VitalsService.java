package com.meditracker.vitalsservice.service;

import com.meditracker.vitalsservice.dto.PatientDTO;
import com.meditracker.vitalsservice.dto.VitalAlertEvent;
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
	private NotificationServiceClient notificationServiceClient;

	public VitalReading submitReading(VitalReading reading) {

		if (reading.getVitalType() == VitalType.BLOOD_PRESSURE) {
			if (reading.getSystolic() == null || reading.getDiastolic() == null) {
				throw new IllegalArgumentException("Systolic and diastolic values are required for blood pressure");
			}
		} else {
			if (reading.getValue() == null) {
				throw new IllegalArgumentException("A reading value is required");
			}
		}

		boolean abnormal = thresholdService.isAbnormal(reading);
		reading.setAlertTriggered(abnormal);

		VitalReading saved = vitalReadingRepository.save(reading);

		if (abnormal) {
			try {
				PatientDTO patient = patientServiceClient.getPatient(reading.getPatientId());

				VitalAlertEvent event = new VitalAlertEvent(patient.getId(), patient.getName(),
						patient.getAssignedDoctorId(), reading.getVitalType().name(),
						thresholdService.formatReadingValue(reading),
						thresholdService.getSafeRange(reading.getVitalType()), reading.getTimestamp());

				notificationServiceClient.sendAlert(event);
				System.out.println("Alert sent for patient: " + patient.getName());

			} catch (Exception e) {
				System.err.println("Failed to send alert: " + e.getMessage());
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