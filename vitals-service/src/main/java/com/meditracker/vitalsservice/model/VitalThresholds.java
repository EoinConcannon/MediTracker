package com.meditracker.vitalsservice.model;

public class VitalThresholds {

	// Blood Pressure (mmHg)
	public static final int BP_SYSTOLIC_MIN = 90;
	public static final int BP_SYSTOLIC_MAX = 139;
	public static final int BP_DIASTOLIC_MIN = 60;
	public static final int BP_DIASTOLIC_MAX = 89;

	// Heart Rate (BPM)
	public static final double HR_MIN = 60;
	public static final double HR_MAX = 100;

	// Blood Glucose (mmol/L)
	public static final double GLUCOSE_MIN = 4.0;
	public static final double GLUCOSE_MAX = 7.8;

	// Temperature (Celsius)
	public static final double TEMP_MIN = 36.1;
	public static final double TEMP_MAX = 37.8;

	// SpO2 (%)
	public static final double SPO2_MIN = 95.0;
}