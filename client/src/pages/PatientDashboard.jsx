import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import Navbar from '../components/Navbar';
import { useAuth } from '../context/AuthContext';
import { PATIENT_SERVICE_URL } from '../config';

export default function PatientDashboard() {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [doctor, setDoctor] = useState(null);

    useEffect(() => {
        const fetchDoctor = async () => {
            try {
                const response = await axios.get(
                    `${PATIENT_SERVICE_URL}/api/doctors/${user.data.assignedDoctorId}`
                );
                setDoctor(response.data);
            } catch (err) {
                console.error('Failed to fetch doctor', err);
            }
        };

        if (user.data.assignedDoctorId) {
            fetchDoctor();
        }
    }, [user.data.assignedDoctorId]);

    return (
        <>
            <Navbar />
            <div className="container mt-4">
                <h2 className="mb-1">Welcome, {user.name}</h2>
                <p className="text-muted mb-4">
                    Manage your health with MediTracker
                </p>

                {/* Assigned Doctor Card */}
                {doctor && (
                    <div className="card mb-4 border-primary shadow-sm">
                        <div className="card-body d-flex
                                        align-items-center gap-3">
                            <div className="bg-primary rounded-circle d-flex
                                            align-items-center
                                            justify-content-center text-white fw-bold"
                                style={{
                                    width: 52, height: 52,
                                    fontSize: 20, flexShrink: 0
                                }}>
                                {doctor.name.charAt(0)}
                            </div>
                            <div>
                                <div className="fw-bold">{doctor.name}</div>
                                <div className="text-muted small">
                                    {doctor.specialisation}
                                </div>
                                <div className="text-muted small">
                                    {doctor.email}
                                </div>
                            </div>
                            <span className="badge bg-primary ms-auto">
                                Your Doctor
                            </span>
                        </div>
                    </div>
                )}

                {/* Quick Actions */}
                <h5 className="mb-3 text-muted">Quick Actions</h5>
                <div className="row">
                    <div className="col-md-4 mb-3">
                        <div className="card h-100 shadow-sm">
                            <div className="card-body">
                                <h5 className="card-title">Submit Vitals</h5>
                                <p className="card-text text-muted">
                                    Record your latest health readings
                                </p>
                                <button
                                    className="btn btn-primary"
                                    onClick={() =>
                                        navigate('/submit-vitals')}>
                                    Submit Reading
                                </button>
                            </div>
                        </div>
                    </div>
                    <div className="col-md-4 mb-3">
                        <div className="card h-100 shadow-sm">
                            <div className="card-body">
                                <h5 className="card-title">Appointments</h5>
                                <p className="card-text text-muted">
                                    Book and manage your appointments
                                </p>
                                <button
                                    className="btn btn-primary"
                                    onClick={() =>
                                        navigate('/appointments')}>
                                    View Appointments
                                </button>
                            </div>
                        </div>
                    </div>
                    <div className="col-md-4 mb-3">
                        <div className="card h-100 shadow-sm">
                            <div className="card-body">
                                <h5 className="card-title">Prescriptions</h5>
                                <p className="card-text text-muted">
                                    View your active prescriptions
                                </p>
                                <button
                                    className="btn btn-primary"
                                    onClick={() =>
                                        navigate('/prescriptions')}>
                                    View Prescriptions
                                </button>
                            </div>
                        </div>
                    </div>
                    <div className="col-md-4 mb-3">
                        <div className="card h-100 shadow-sm">
                            <div className="card-body">
                                <h5 className="card-title">Vital History</h5>
                                <p className="card-text text-muted">
                                    View all your past vital readings
                                </p>
                                <button
                                    className="btn btn-primary"
                                    onClick={() =>
                                        navigate('/vital-history')}>
                                    View History
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}