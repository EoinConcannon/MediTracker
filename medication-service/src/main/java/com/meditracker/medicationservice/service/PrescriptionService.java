package com.meditracker.medicationservice.service;

import com.meditracker.medicationservice.dto.PatientDTO;
import com.meditracker.medicationservice.model.Prescription;
import com.meditracker.medicationservice.model.PrescriptionStatus;
import com.meditracker.medicationservice.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrescriptionService {

	@Autowired
	private PrescriptionRepository prescriptionRepository;

	@Autowired
	private PatientServiceClient patientServiceClient;

	public Prescription issuePrescription(Prescription prescription) {

		// Fetch patient to check allergies
		try {
			PatientDTO patient = patientServiceClient.getPatient(prescription.getPatientId());

			if (patient != null && patient.getAllergies() != null && !patient.getAllergies().isBlank()
					&& !patient.getAllergies().equalsIgnoreCase("none")) {

				String allergies = patient.getAllergies().toLowerCase();
				String drugName = prescription.getDrugName().toLowerCase();

				// Simple check — if drug name appears in allergy list
				if (allergies.contains(drugName)) {
					prescription.setAllergyWarning(true);
				}
			}
		} catch (Exception e) {
			// If patient service is unavailable, proceed without allergy check
			System.err.println("Could not check allergies: " + e.getMessage());
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