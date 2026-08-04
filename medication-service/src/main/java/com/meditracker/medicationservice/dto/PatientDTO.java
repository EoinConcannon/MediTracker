package com.meditracker.medicationservice.dto;

public class PatientDTO {
	private Long id;
	private String name;
	private String allergies;
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

	public String getAllergies() {
		return allergies;
	}

	public void setAllergies(String allergies) {
		this.allergies = allergies;
	}

	public Long getAssignedDoctorId() {
		return assignedDoctorId;
	}

	public void setAssignedDoctorId(Long assignedDoctorId) {
		this.assignedDoctorId = assignedDoctorId;
	}
}