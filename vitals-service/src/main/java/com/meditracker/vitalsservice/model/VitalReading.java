package com.meditracker.vitalsservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "vital_readings")
public class VitalReading {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "Patient ID is required")
	private Long patientId;

	@NotNull(message = "Vital type is required")
	@Enumerated(EnumType.STRING)
	private VitalType vitalType;

	// Used for HR, Glucose, Temperature, SpO2
	private Double value;

	// Used for Blood Pressure only
	private Integer systolic;
	private Integer diastolic;

	private LocalDateTime timestamp;
	private Boolean alertTriggered = false;

	@PrePersist
	public void prePersist() {
		if (timestamp == null) {
			timestamp = LocalDateTime.now();
		}
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

	public VitalType getVitalType() {
		return vitalType;
	}

	public void setVitalType(VitalType vitalType) {
		this.vitalType = vitalType;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public Integer getSystolic() {
		return systolic;
	}

	public void setSystolic(Integer systolic) {
		this.systolic = systolic;
	}

	public Integer getDiastolic() {
		return diastolic;
	}

	public void setDiastolic(Integer diastolic) {
		this.diastolic = diastolic;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public Boolean getAlertTriggered() {
		return alertTriggered;
	}

	public void setAlertTriggered(Boolean alertTriggered) {
		this.alertTriggered = alertTriggered;
	}
}