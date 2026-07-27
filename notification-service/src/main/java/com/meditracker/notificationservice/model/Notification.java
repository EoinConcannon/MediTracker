package com.meditracker.notificationservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long doctorId;
	private Long patientId;
	private String patientName;
	private String vitalType;
	private String readingValue;
	private String safeRange;
	private String message;
	private LocalDateTime timestamp;

	@Enumerated(EnumType.STRING)
	private NotificationStatus status = NotificationStatus.UNREAD;

	public Notification() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getDoctorId() {
		return doctorId;
	}

	public void setDoctorId(Long doctorId) {
		this.doctorId = doctorId;
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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public NotificationStatus getStatus() {
		return status;
	}

	public void setStatus(NotificationStatus status) {
		this.status = status;
	}
}