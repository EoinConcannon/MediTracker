package com.meditracker.medicationservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "prescriptions")
public class Prescription {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "Patient ID is required")
	private Long patientId;

	@NotNull(message = "Doctor ID is required")
	private Long doctorId;

	@NotBlank(message = "Drug name is required")
	private String drugName;

	@NotBlank(message = "Dosage is required")
	private String dosage;

	@NotBlank(message = "Frequency is required")
	private String frequency;

	@NotNull(message = "Start date is required")
	private LocalDate startDate;

	private LocalDate endDate;

	@Enumerated(EnumType.STRING)
	private PrescriptionStatus status = PrescriptionStatus.ACTIVE;

	private boolean allergyWarning = false;

	public Prescription() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getPatientId() {
		return patientId;
	}

	public void setPatientId(Long patientId) {
		this.patientId = patientId;
	}

	public Long getDoctorId() {
		return doctorId;
	}

	public void setDoctorId(Long doctorId) {
		this.doctorId = doctorId;
	}

	public String getDrugName() {
		return drugName;
	}

	public void setDrugName(String drugName) {
		this.drugName = drugName;
	}

	public String getDosage() {
		return dosage;
	}

	public void setDosage(String dosage) {
		this.dosage = dosage;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public PrescriptionStatus getStatus() {
		return status;
	}

	public void setStatus(PrescriptionStatus status) {
		this.status = status;
	}

	public boolean isAllergyWarning() {
		return allergyWarning;
	}

	public void setAllergyWarning(boolean allergyWarning) {
		this.allergyWarning = allergyWarning;
	}
}