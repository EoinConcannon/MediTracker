import { useState } from 'react';
import axios from 'axios';
import Navbar from '../components/Navbar';
import { useAuth } from '../context/AuthContext';
import { VITALS_SERVICE_URL } from '../config';

export default function SubmitVitals() {
    const { user } = useAuth();
    const [vitalType, setVitalType] = useState('BLOOD_PRESSURE');
    const [value, setValue] = useState('');
    const [systolic, setSystolic] = useState('');
    const [diastolic, setDiastolic] = useState('');
    const [result, setResult] = useState(null);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setResult(null);

        // Build the request body
        const body = { patientId: user.id, vitalType };

        if (vitalType === 'BLOOD_PRESSURE') {
            if (!systolic || !diastolic) {
                setError('Please enter both systolic and diastolic values');
                return;
            }
            body.systolic = parseInt(systolic);
            body.diastolic = parseInt(diastolic);
        } else {
            if (!value) {
                setError('Please enter a reading value');
                return;
            }
            body.value = parseFloat(value);
        }

        setLoading(true);

        try {
            const response = await axios.post(
                `${VITALS_SERVICE_URL}/api/vitals`,
                body
            );
            setResult(response.data);
        } catch (err) {
            if (err.response && err.response.data) {
                setError(err.response.data.message || 'Something went wrong');
            } else {
                setError('Could not connect to the vitals service');
            }
        } finally {
            setLoading(false);
        }
    };

    const resetForm = () => {
        setValue('');
        setSystolic('');
        setDiastolic('');
        setResult(null);
        setError('');
    };

    return (
        <>
            <Navbar />
            <div className="container mt-4" style={{ maxWidth: '600px' }}>
                <h2 className="mb-4">Submit Vital Reading</h2>

                {/* Result message */}
                {result && (
                    <div className={`alert ${result.alertTriggered
                        ? 'alert-danger' : 'alert-success'} mb-4`}>
                        {result.alertTriggered ? (
                            <>
                                <strong>⚠ Alert triggered</strong> — your reading
                                was outside the safe range. Your doctor has been
                                notified automatically.
                            </>
                        ) : (
                            <>
                                <strong>✓ Reading saved</strong> — your reading
                                is within the normal range.
                            </>
                        )}
                        <div className="mt-2">
                            <button
                                className="btn btn-sm btn-outline-secondary"
                                onClick={resetForm}>
                                Submit another reading
                            </button>
                        </div>
                    </div>
                )}

                {/* Form */}
                {!result && (
                    <div className="card p-4 shadow-sm">
                        <form onSubmit={handleSubmit}>

                            <div className="mb-3">
                                <label className="form-label">Vital Type</label>
                                <select
                                    className="form-select"
                                    value={vitalType}
                                    onChange={(e) => {
                                        setVitalType(e.target.value);
                                        setValue('');
                                        setSystolic('');
                                        setDiastolic('');
                                        setError('');
                                    }}>
                                    <option value="BLOOD_PRESSURE">
                                        Blood Pressure
                                    </option>
                                    <option value="HEART_RATE">Heart Rate</option>
                                    <option value="GLUCOSE">Blood Glucose</option>
                                    <option value="TEMPERATURE">Temperature</option>
                                    <option value="SPO2">Oxygen Saturation (SpO2)</option>
                                </select>
                            </div>

                            {/* Blood pressure needs two fields */}
                            {vitalType === 'BLOOD_PRESSURE' ? (
                                <div className="row mb-3">
                                    <div className="col">
                                        <label className="form-label">
                                            Systolic (mmHg)
                                        </label>
                                        <input
                                            type="number"
                                            className="form-control"
                                            placeholder="e.g. 120"
                                            value={systolic}
                                            onChange={(e) =>
                                                setSystolic(e.target.value)}
                                        />
                                    </div>
                                    <div className="col">
                                        <label className="form-label">
                                            Diastolic (mmHg)
                                        </label>
                                        <input
                                            type="number"
                                            className="form-control"
                                            placeholder="e.g. 80"
                                            value={diastolic}
                                            onChange={(e) =>
                                                setDiastolic(e.target.value)}
                                        />
                                    </div>
                                </div>
                            ) : (
                                <div className="mb-3">
                                    <label className="form-label">
                                        {vitalType === 'HEART_RATE' && 'Heart Rate (BPM)'}
                                        {vitalType === 'GLUCOSE' && 'Blood Glucose (mmol/L)'}
                                        {vitalType === 'TEMPERATURE' && 'Temperature (°C)'}
                                        {vitalType === 'SPO2' && 'Oxygen Saturation (%)'}
                                    </label>
                                    <input
                                        type="number"
                                        step="0.1"
                                        className="form-control"
                                        placeholder={
                                            vitalType === 'HEART_RATE' ? 'e.g. 75' :
                                                vitalType === 'GLUCOSE' ? 'e.g. 5.5' :
                                                    vitalType === 'TEMPERATURE' ? 'e.g. 36.8' :
                                                        'e.g. 98'
                                        }
                                        value={value}
                                        onChange={(e) => setValue(e.target.value)}
                                    />
                                </div>
                            )}

                            {/* Safe range hint */}
                            <div className="text-muted small mb-3">
                                {vitalType === 'BLOOD_PRESSURE' &&
                                    'Safe range: Systolic 90-139 mmHg, Diastolic 60-89 mmHg'}
                                {vitalType === 'HEART_RATE' &&
                                    'Safe range: 60-100 BPM'}
                                {vitalType === 'GLUCOSE' &&
                                    'Safe range: 4.0-7.8 mmol/L'}
                                {vitalType === 'TEMPERATURE' &&
                                    'Safe range: 36.1-37.8°C'}
                                {vitalType === 'SPO2' &&
                                    'Safe range: 95-100%'}
                            </div>

                            {error && (
                                <div className="alert alert-danger py-2">
                                    {error}
                                </div>
                            )}

                            <button
                                type="submit"
                                className="btn btn-primary w-100"
                                disabled={loading}>
                                {loading ? 'Submitting...' : 'Submit Reading'}
                            </button>
                        </form>
                    </div>
                )}
            </div>
        </>
    );
}