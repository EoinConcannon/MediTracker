package com.meditracker.vitalsservice.service;

import com.meditracker.vitalsservice.model.VitalReading;
import com.meditracker.vitalsservice.model.VitalThresholds;
import com.meditracker.vitalsservice.model.VitalType;
import org.springframework.stereotype.Service;

@Service
public class ThresholdService {

	public boolean isAbnormal(VitalReading reading) {
		return switch (reading.getVitalType()) {
		case BLOOD_PRESSURE -> isBloodPressureAbnormal(reading);
		case HEART_RATE -> reading.getValue() < VitalThresholds.HR_MIN || reading.getValue() > VitalThresholds.HR_MAX;
		case GLUCOSE ->
			reading.getValue() < VitalThresholds.GLUCOSE_MIN || reading.getValue() > VitalThresholds.GLUCOSE_MAX;
		case TEMPERATURE ->
			reading.getValue() < VitalThresholds.TEMP_MIN || reading.getValue() > VitalThresholds.TEMP_MAX;
		case SPO2 -> reading.getValue() < VitalThresholds.SPO2_MIN;
		};
	}

	private boolean isBloodPressureAbnormal(VitalReading reading) {
		return reading.getSystolic() > VitalThresholds.BP_SYSTOLIC_MAX
				|| reading.getSystolic() < VitalThresholds.BP_SYSTOLIC_MIN
				|| reading.getDiastolic() > VitalThresholds.BP_DIASTOLIC_MAX
				|| reading.getDiastolic() < VitalThresholds.BP_DIASTOLIC_MIN;
	}

	public String getSafeRange(VitalType type) {
		return switch (type) {
		case BLOOD_PRESSURE -> "Systolic 90-139 mmHg, Diastolic 60-89 mmHg";
		case HEART_RATE -> "60-100 BPM";
		case GLUCOSE -> "4.0-7.8 mmol/L";
		case TEMPERATURE -> "36.1-37.8°C";
		case SPO2 -> "95-100%";
		};
	}

	public String formatReadingValue(VitalReading reading) {
		if (reading.getVitalType() == VitalType.BLOOD_PRESSURE) {
			return reading.getSystolic() + "/" + reading.getDiastolic() + " mmHg";
		}
		return switch (reading.getVitalType()) {
		case HEART_RATE -> reading.getValue() + " BPM";
		case GLUCOSE -> reading.getValue() + " mmol/L";
		case TEMPERATURE -> reading.getValue() + "°C";
		case SPO2 -> reading.getValue() + "%";
		default -> reading.getValue().toString();
		};
	}
}