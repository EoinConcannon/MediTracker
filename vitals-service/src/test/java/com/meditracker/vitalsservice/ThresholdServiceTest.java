package com.meditracker.vitalsservice;

import com.meditracker.vitalsservice.model.VitalReading;
import com.meditracker.vitalsservice.model.VitalType;
import com.meditracker.vitalsservice.service.ThresholdService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ThresholdServiceTest {

	@InjectMocks
	private ThresholdService thresholdService;

	// ─── Blood Pressure ──────────────────────────────────────────────────────

	@Test
	void isAbnormal_BloodPressure_Normal_ReturnsFalse() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.BLOOD_PRESSURE);
		reading.setSystolic(120);
		reading.setDiastolic(80);

		assertFalse(thresholdService.isAbnormal(reading));
	}

	@Test
	void isAbnormal_BloodPressure_HighSystolic_ReturnsTrue() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.BLOOD_PRESSURE);
		reading.setSystolic(155);
		reading.setDiastolic(80);

		assertTrue(thresholdService.isAbnormal(reading));
	}

	@Test
	void isAbnormal_BloodPressure_LowSystolic_ReturnsTrue() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.BLOOD_PRESSURE);
		reading.setSystolic(85);
		reading.setDiastolic(80);

		assertTrue(thresholdService.isAbnormal(reading));
	}

	@Test
	void isAbnormal_BloodPressure_HighDiastolic_ReturnsTrue() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.BLOOD_PRESSURE);
		reading.setSystolic(120);
		reading.setDiastolic(95);

		assertTrue(thresholdService.isAbnormal(reading));
	}

	@Test
	void isAbnormal_BloodPressure_LowDiastolic_ReturnsTrue() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.BLOOD_PRESSURE);
		reading.setSystolic(120);
		reading.setDiastolic(55);

		assertTrue(thresholdService.isAbnormal(reading));
	}

	@Test
	void isAbnormal_BloodPressure_AtExactUpperBoundary_ReturnsFalse() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.BLOOD_PRESSURE);
		reading.setSystolic(139);
		reading.setDiastolic(89);

		assertFalse(thresholdService.isAbnormal(reading));
	}

	@Test
	void isAbnormal_BloodPressure_OneOverUpperBoundary_ReturnsTrue() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.BLOOD_PRESSURE);
		reading.setSystolic(140);
		reading.setDiastolic(80);

		assertTrue(thresholdService.isAbnormal(reading));
	}

	// ─── Heart Rate ──────────────────────────────────────────────────────────

	@Test
	void isAbnormal_HeartRate_Normal_ReturnsFalse() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.HEART_RATE);
		reading.setValue(75.0);

		assertFalse(thresholdService.isAbnormal(reading));
	}

	@Test
	void isAbnormal_HeartRate_TooHigh_ReturnsTrue() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.HEART_RATE);
		reading.setValue(115.0);

		assertTrue(thresholdService.isAbnormal(reading));
	}

	@Test
	void isAbnormal_HeartRate_TooLow_ReturnsTrue() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.HEART_RATE);
		reading.setValue(45.0);

		assertTrue(thresholdService.isAbnormal(reading));
	}

	@Test
	void isAbnormal_HeartRate_AtExactLowerBoundary_ReturnsFalse() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.HEART_RATE);
		reading.setValue(60.0);

		assertFalse(thresholdService.isAbnormal(reading));
	}

	@Test
	void isAbnormal_HeartRate_AtExactUpperBoundary_ReturnsFalse() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.HEART_RATE);
		reading.setValue(100.0);

		assertFalse(thresholdService.isAbnormal(reading));
	}

	// ─── Glucose ─────────────────────────────────────────────────────────────

	@Test
	void isAbnormal_Glucose_Normal_ReturnsFalse() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.GLUCOSE);
		reading.setValue(5.5);

		assertFalse(thresholdService.isAbnormal(reading));
	}

	@Test
	void isAbnormal_Glucose_TooHigh_ReturnsTrue() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.GLUCOSE);
		reading.setValue(9.5);

		assertTrue(thresholdService.isAbnormal(reading));
	}

	@Test
	void isAbnormal_Glucose_TooLow_ReturnsTrue() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.GLUCOSE);
		reading.setValue(3.0);

		assertTrue(thresholdService.isAbnormal(reading));
	}

	@Test
	void isAbnormal_Glucose_AtExactUpperBoundary_ReturnsFalse() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.GLUCOSE);
		reading.setValue(7.8);

		assertFalse(thresholdService.isAbnormal(reading));
	}

	@Test
	void isAbnormal_Glucose_AtExactLowerBoundary_ReturnsFalse() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.GLUCOSE);
		reading.setValue(4.0);

		assertFalse(thresholdService.isAbnormal(reading));
	}

	// ─── Temperature ─────────────────────────────────────────────────────────

	@Test
	void isAbnormal_Temperature_Normal_ReturnsFalse() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.TEMPERATURE);
		reading.setValue(36.8);

		assertFalse(thresholdService.isAbnormal(reading));
	}

	@Test
	void isAbnormal_Temperature_Fever_ReturnsTrue() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.TEMPERATURE);
		reading.setValue(38.5);

		assertTrue(thresholdService.isAbnormal(reading));
	}

	@Test
	void isAbnormal_Temperature_TooLow_ReturnsTrue() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.TEMPERATURE);
		reading.setValue(35.5);

		assertTrue(thresholdService.isAbnormal(reading));
	}

	@Test
	void isAbnormal_Temperature_AtExactUpperBoundary_ReturnsFalse() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.TEMPERATURE);
		reading.setValue(37.8);

		assertFalse(thresholdService.isAbnormal(reading));
	}

	// ─── SpO2 ────────────────────────────────────────────────────────────────

	@Test
	void isAbnormal_SpO2_Normal_ReturnsFalse() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.SPO2);
		reading.setValue(98.0);

		assertFalse(thresholdService.isAbnormal(reading));
	}

	@Test
	void isAbnormal_SpO2_TooLow_ReturnsTrue() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.SPO2);
		reading.setValue(92.0);

		assertTrue(thresholdService.isAbnormal(reading));
	}

	@Test
	void isAbnormal_SpO2_AtExactBoundary_ReturnsFalse() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.SPO2);
		reading.setValue(95.0);

		assertFalse(thresholdService.isAbnormal(reading));
	}

	@Test
	void isAbnormal_SpO2_OneBelowBoundary_ReturnsTrue() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.SPO2);
		reading.setValue(94.9);

		assertTrue(thresholdService.isAbnormal(reading));
	}

	// ─── getSafeRange ────────────────────────────────────────────────────────

	@Test
	void getSafeRange_BloodPressure_ReturnsCorrectString() {
		assertEquals("Systolic 90-139 mmHg, Diastolic 60-89 mmHg",
				thresholdService.getSafeRange(VitalType.BLOOD_PRESSURE));
	}

	@Test
	void getSafeRange_HeartRate_ReturnsCorrectString() {
		assertEquals("60-100 BPM", thresholdService.getSafeRange(VitalType.HEART_RATE));
	}

	@Test
	void getSafeRange_Glucose_ReturnsCorrectString() {
		assertEquals("4.0-7.8 mmol/L", thresholdService.getSafeRange(VitalType.GLUCOSE));
	}

	@Test
	void getSafeRange_Temperature_ReturnsCorrectString() {
		assertEquals("36.1-37.8°C", thresholdService.getSafeRange(VitalType.TEMPERATURE));
	}

	@Test
	void getSafeRange_SpO2_ReturnsCorrectString() {
		assertEquals("95-100%", thresholdService.getSafeRange(VitalType.SPO2));
	}

	// ─── formatReadingValue ──────────────────────────────────────────────────

	@Test
	void formatReadingValue_BloodPressure_FormatsSystolicDiastolic() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.BLOOD_PRESSURE);
		reading.setSystolic(120);
		reading.setDiastolic(80);

		assertEquals("120/80 mmHg", thresholdService.formatReadingValue(reading));
	}

	@Test
	void formatReadingValue_HeartRate_AppendsBPM() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.HEART_RATE);
		reading.setValue(75.0);

		assertEquals("75.0 BPM", thresholdService.formatReadingValue(reading));
	}

	@Test
	void formatReadingValue_Glucose_AppendsMmolL() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.GLUCOSE);
		reading.setValue(5.5);

		assertEquals("5.5 mmol/L", thresholdService.formatReadingValue(reading));
	}

	@Test
	void formatReadingValue_Temperature_AppendsDegrees() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.TEMPERATURE);
		reading.setValue(36.8);

		assertEquals("36.8°C", thresholdService.formatReadingValue(reading));
	}

	@Test
	void formatReadingValue_SpO2_AppendsPercent() {
		VitalReading reading = new VitalReading();
		reading.setVitalType(VitalType.SPO2);
		reading.setValue(98.0);

		assertEquals("98.0%", thresholdService.formatReadingValue(reading));
	}
}