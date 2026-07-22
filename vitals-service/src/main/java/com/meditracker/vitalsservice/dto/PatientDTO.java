package com.meditracker.vitalsservice.dto;

public class PatientDTO {
	private Long id;
	private String name;
	private Long assignedDoctorId;

	public PatientDTO() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getAssignedDoctorId() {
		return assignedDoctorId;
	}

	public void setAssignedDoctorId(Long assignedDoctorId) {
		this.assignedDoctorId = assignedDoctorId;
	}
}