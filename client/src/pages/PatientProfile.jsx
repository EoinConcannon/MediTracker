import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import Navbar from '../components/Navbar';
import { useAuth } from '../context/AuthContext';
import { PATIENT_SERVICE_URL } from '../config';

export default function PatientProfile() {
    const { id } = useParams();
    const { user } = useAuth();
    const [patient, setPatient] = useState(null);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchPatient = async () => {
            try {
                const response = await axios.get(
                    `${PATIENT_SERVICE_URL}/api/patients/${id}`,
                    { params: { doctorId: user.id } }
                );
                setPatient(response.data);
            } catch (err) {
                if (err.response && err.response.status === 403) {
                    setError('You are not authorised to view this patient');
                } else if (err.response && err.response.status === 404) {
                    setError('Patient not found');
                } else {
                    setError('Something went wrong');
                }
            } finally {
                setLoading(false);
            }
        };

        fetchPatient();
    }, [id, user.id]);

    return (
        <>
            <Navbar />
            <div className="container mt-4">

                {loading && (
                    <p className="text-muted">Loading patient profile...</p>
                )}

                {error && (
                    <div className="alert alert-danger">{error}</div>
                )}

                {patient && (
                    <>
                        <h2 className="mb-4">{patient.name}</h2>

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
                    </>
                )}
            </div>
        </>
    );
}