package com.meditracker.vitalsservice.dto;

import java.time.LocalDateTime;

public class VitalAlertEvent {

	private Long patientId;
	private String patientName;
	private Long doctorId;
	private String vitalType;
	private String readingValue;
	private String safeRange;
	private LocalDateTime timestamp;

	public VitalAlertEvent() {
	}

	public VitalAlertEvent(Long patientId, String patientName, Long doctorId, String vitalType, String readingValue,
			String safeRange, LocalDateTime timestamp) {
		this.patientId = patientId;
		this.patientName = patientName;
		this.doctorId = doctorId;
		this.vitalType = vitalType;
		this.readingValue = readingValue;
		this.safeRange = safeRange;
		this.timestamp = timestamp;
	}

	public Long getPatientId() {
		return patientId;
	}

	public void setPatientId(Long patientId) {
		this.patientId = patientId;
	}

	public String getPatientName() {
		return patientName;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public Long getDoctorId() {
		return doctorId;
	}

	public void setDoctorId(Long doctorId) {
		this.doctorId = doctorId;
	}

	public String getVitalType() {
		return vitalType;
	}

	public void setVitalType(String vitalType) {
		this.vitalType = vitalType;
	}

	public String getReadingValue() {
		return readingValue;
	}

	public void setReadingValue(String readingValue) {
		this.readingValue = readingValue;
	}

	public String getSafeRange() {
		return safeRange;
	}

	public void setSafeRange(String safeRange) {
		this.safeRange = safeRange;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
}