package com.meditracker.vitalsservice.service;

import com.meditracker.vitalsservice.dto.PatientDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PatientServiceClient {

	private final RestTemplate restTemplate;

	@Value("${patient.service.url:http://localhost:8081}")
	private String patientServiceUrl;

	public PatientServiceClient(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public PatientDTO getPatient(Long patientId) {
		String url = patientServiceUrl + "/api/patients/" + patientId;
		return restTemplate.getForObject(url, PatientDTO.class);
	}
}