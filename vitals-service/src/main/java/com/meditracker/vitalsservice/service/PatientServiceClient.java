package com.meditracker.vitalsservice.service;

import com.meditracker.vitalsservice.dto.PatientDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PatientServiceClient {

	@Autowired
	private RestTemplate restTemplate;

	@Value("${patient.service.url:http://localhost:8081}")
	private String patientServiceUrl;

	public PatientDTO getPatient(Long patientId) {
		String url = patientServiceUrl + "/api/patients/" + patientId;
		return restTemplate.getForObject(url, PatientDTO.class);
	}
}