package com.meditracker.patientservice.service;

import com.meditracker.patientservice.model.Patient;
import com.meditracker.patientservice.model.Prescription;
import com.meditracker.patientservice.model.PrescriptionStatus;
import com.meditracker.patientservice.repository.PatientRepository;
import com.meditracker.patientservice.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PrescriptionService {

	@Autowired
	private PrescriptionRepository prescriptionRepository;

	@Autowired
	private PatientRepository patientRepository;

	public Prescription issuePrescription(Prescription prescription) {

		// Check allergies directly — no inter-service call needed anymore
		Optional<Patient> patientOpt = patientRepository.findById(prescription.getPatientId());

		if (patientOpt.isPresent()) {
			Patient patient = patientOpt.get();
			if (patient.getAllergies() != null && !patient.getAllergies().isBlank()
					&& !patient.getAllergies().equalsIgnoreCase("none")) {

				String allergies = patient.getAllergies().toLowerCase();
				String drugName = prescription.getDrugName().toLowerCase();

				if (allergies.contains(drugName)) {
					prescription.setAllergyWarning(true);
				}
			}
		}

		prescription.setStatus(PrescriptionStatus.ACTIVE);
		return prescriptionRepository.save(prescription);
	}

	public List<Prescription> getPrescriptionsByPatient(Long patientId) {
		return prescriptionRepository.findByPatientId(patientId);
	}

	public List<Prescription> getActivePrescriptionsByPatient(Long patientId) {
		return prescriptionRepository.findByPatientIdAndStatus(patientId, PrescriptionStatus.ACTIVE);
	}

	public List<Prescription> getPrescriptionsByDoctor(Long doctorId) {
		return prescriptionRepository.findByDoctorId(doctorId);
	}
}