import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import Navbar from '../components/Navbar';
import { useAuth } from '../context/AuthContext';
import { PATIENT_SERVICE_URL, VITALS_SERVICE_URL } from '../config';

export default function PatientProfile() {
    const { id } = useParams();
    const { user } = useAuth();
    const navigate = useNavigate();
    const [patient, setPatient] = useState(null);
    const [vitals, setVitals] = useState([]);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const patientRes = await axios.get(
                    `${PATIENT_SERVICE_URL}/api/patients/${id}`,
                    { params: { doctorId: user.id } }
                );
                setPatient(patientRes.data);

                const vitalsRes = await axios.get(
                    `${VITALS_SERVICE_URL}/api/vitals`,
                    { params: { patientId: id } }
                );
                setVitals(vitalsRes.data);

            } catch (err) {
                if (err.response?.status === 403) {
                    setError('You are not authorised to view this patient');
                } else if (err.response?.status === 404) {
                    setError('Patient not found');
                } else {
                    setError('Something went wrong');
                }
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [id, user.id]);

    const formatVitalType = (type) =>
        type.replace(/_/g, ' ').toLowerCase()
            .replace(/\b\w/g, c => c.toUpperCase());

    const formatReading = (vital) => {
        if (vital.vitalType === 'BLOOD_PRESSURE') {
            return `${vital.systolic}/${vital.diastolic} mmHg`;
        }
        const units = {
            HEART_RATE: 'BPM',
            GLUCOSE: 'mmol/L',
            TEMPERATURE: '°C',
            SPO2: '%'
        };
        return `${vital.value} ${units[vital.vitalType] || ''}`;
    };

    return (
        <>
            <Navbar />
            <div className="container mt-4">

                <button
                    className="btn btn-outline-secondary btn-sm mb-3"
                    onClick={() => navigate(-1)}>
                    ← Back
                </button>

                {loading && <p className="text-muted">Loading...</p>}
                {error && <div className="alert alert-danger">{error}</div>}

                {patient && (
                    <>
                        <h2 className="mb-4">{patient.name}</h2>

                        {/* Personal Details */}
                        <div className="card mb-4">
                            <div className="card-header bg-primary text-white fw-bold">
                                Personal Details
                            </div>
                            <div className="card-body">
                                <div className="row mb-2">
                                    <div className="col-4 fw-bold">Date of Birth</div>
                                    <div className="col-8">{patient.dateOfBirth}</div>
                                </div>
                                <div className="row mb-2">
                                    <div className="col-4 fw-bold">Email</div>
                                    <div className="col-8">{patient.email}</div>
                                </div>
                                <div className="row mb-2">
                                    <div className="col-4 fw-bold">Phone</div>
                                    <div className="col-8">
                                        {patient.phone || 'Not provided'}
                                    </div>
                                </div>
                                <div className="row mb-2">
                                    <div className="col-4 fw-bold">Address</div>
                                    <div className="col-8">
                                        {patient.address || 'Not provided'}
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* Medical Information */}
                        <div className="card mb-4">
                            <div className="card-header bg-primary text-white fw-bold">
                                Medical Information
                            </div>
                            <div className="card-body">
                                <div className="row mb-2">
                                    <div className="col-4 fw-bold">Medical History</div>
                                    <div className="col-8">
                                        {patient.medicalHistory || 'None recorded'}
                                    </div>
                                </div>
                                <div className="row mb-2">
                                    <div className="col-4 fw-bold">Allergies</div>
                                    <div className="col-8 text-danger fw-bold">
                                        {patient.allergies || 'None recorded'}
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* Vital Readings History */}
                        <div className="card mb-4">
                            <div className="card-header bg-primary text-white fw-bold">
                                Vital Readings History
                            </div>
                            <div className="card-body p-0">
                                {vitals.length === 0 ? (
                                    <p className="text-muted p-3 mb-0">
                                        No readings submitted yet.
                                    </p>
                                ) : (
                                    <table className="table table-hover mb-0">
                                        <thead className="table-light">
                                            <tr>
                                                <th>Type</th>
                                                <th>Reading</th>
                                                <th>Date & Time</th>
                                                <th>Status</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {vitals
                                                .sort((a, b) =>
                                                    new Date(b.timestamp) -
                                                    new Date(a.timestamp)
                                                )
                                                .map(vital => (
                                                    <tr key={vital.id}>
                                                        <td>
                                                            {formatVitalType(vital.vitalType)}
                                                        </td>
                                                        <td className="fw-bold">
                                                            {formatReading(vital)}
                                                        </td>
                                                        <td>
                                                            {new Date(vital.timestamp)
                                                                .toLocaleString()}
                                                        </td>
                                                        <td>
                                                            {vital.alertTriggered ? (
                                                                <span className="badge bg-danger">
                                                                    ⚠ Alert
                                                                </span>
                                                            ) : (
                                                                <span className="badge bg-success">
                                                                    Normal
                                                                </span>
                                                            )}
                                                        </td>
                                                    </tr>
                                                ))}
                                        </tbody>
                                    </table>
                                )}
                            </div>
                        </div>
                    </>
                )}
            </div>
        </>
    );
}