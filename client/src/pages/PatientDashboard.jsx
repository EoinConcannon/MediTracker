import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import Navbar from '../components/Navbar';
import { useAuth } from '../context/AuthContext';
import {
    PATIENT_SERVICE_URL,
    VITALS_SERVICE_URL,
    APPOINTMENT_SERVICE_URL,
    MEDICATION_SERVICE_URL
} from '../config';

export default function PatientDashboard() {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [doctor, setDoctor] = useState(null);
    const [alertReadings, setAlertReadings] = useState(0);
    const [pendingAppointments, setPendingAppointments] = useState(0);
    const [allergyWarnings, setAllergyWarnings] = useState(0);

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

        const fetchAlerts = async () => {
            try {
                const [vitalsRes, appointmentsRes, prescriptionsRes] =
                    await Promise.all([
                        axios.get(`${VITALS_SERVICE_URL}/api/vitals`,
                            { params: { patientId: user.id } }),
                        axios.get(`${APPOINTMENT_SERVICE_URL}/api/appointments`,
                            { params: { patientId: user.id } }),
                        axios.get(`${MEDICATION_SERVICE_URL}/api/prescriptions`,
                            { params: { patientId: user.id } })
                    ]);

                setAlertReadings(
                    vitalsRes.data.filter(r => r.alertTriggered).length
                );
                setPendingAppointments(
                    appointmentsRes.data.filter(
                        a => a.status === 'PENDING'
                    ).length
                );
                setAllergyWarnings(
                    prescriptionsRes.data.filter(
                        p => p.allergyWarning
                    ).length
                );
            } catch (err) {
                console.error('Failed to fetch alerts', err);
            }
        };

        if (user.data.assignedDoctorId) fetchDoctor();
        fetchAlerts();
    }, [user.id, user.data.assignedDoctorId]);

    const BadgeCard = ({ title, description, route, buttonText,
        badgeCount, badgeColour = 'danger' }) => (
        <div className="col-md-4 mb-3">
            <div className={`card h-100 shadow-sm position-relative ${badgeCount > 0 ? `border-${badgeColour}` : ''
                }`}>
                {badgeCount > 0 && (
                    <span className={`position-absolute top-0 end-0
                        translate-middle badge rounded-pill
                        bg-${badgeColour}`}
                        style={{ zIndex: 1 }}>
                        {badgeCount}
                    </span>
                )}
                <div className="card-body">
                    <h5 className="card-title">{title}</h5>
                    <p className="card-text text-muted">{description}</p>
                    <button
                        className="btn btn-primary"
                        onClick={() => navigate(route)}>
                        {buttonText}
                    </button>
                </div>
            </div>
        </div>
    );

    return (
        <>
            <Navbar />
            <div className="container mt-4">
                <h2 className="mb-1">Welcome, {user.name}</h2>
                <p className="text-muted mb-4">
                    Manage your health with MediTracker
                </p>

                {doctor && (
                    <div className="card mb-4 border-primary shadow-sm">
                        <div className="card-body d-flex
                                        align-items-center gap-3">
                            <div className="bg-primary rounded-circle d-flex
                                            align-items-center
                                            justify-content-center
                                            text-white fw-bold"
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

                <h5 className="mb-3 text-muted">Quick Actions</h5>
                <div className="row">
                    <BadgeCard
                        title="Submit Vitals"
                        description="Record your latest health readings"
                        route="/submit-vitals"
                        buttonText="Submit Reading"
                        badgeCount={alertReadings}
                        badgeColour="danger"
                    />
                    <BadgeCard
                        title="Appointments"
                        description="Book and manage your appointments"
                        route="/appointments"
                        buttonText="View Appointments"
                        badgeCount={pendingAppointments}
                        badgeColour="warning"
                    />
                    <BadgeCard
                        title="Prescriptions"
                        description="View your active prescriptions"
                        route="/prescriptions"
                        buttonText="View Prescriptions"
                        badgeCount={allergyWarnings}
                        badgeColour="warning"
                    />
                    <BadgeCard
                        title="Vital History"
                        description="View all your past vital readings"
                        route="/vital-history"
                        buttonText="View History"
                        badgeCount={0}
                    />
                </div>
            </div>
        </>
    );
}