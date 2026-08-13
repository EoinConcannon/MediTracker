import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import Navbar from '../components/Navbar';
import { useAuth } from '../context/AuthContext';
import {
    NOTIFICATION_SERVICE_URL,
    APPOINTMENT_SERVICE_URL
} from '../config';

export default function DoctorDashboard() {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [unreadNotifications, setUnreadNotifications] = useState([]);
    const [pendingCount, setPendingCount] = useState(0);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchAlerts = async () => {
            try {
                const [notifRes, pendingRes] = await Promise.all([
                    axios.get(
                        `${NOTIFICATION_SERVICE_URL}/api/notifications/unread`,
                        { params: { doctorId: user.id } }
                    ),
                    axios.get(
                        `${APPOINTMENT_SERVICE_URL}/api/appointments/pending`,
                        { params: { doctorId: user.id } }
                    )
                ]);
                setUnreadNotifications(notifRes.data);
                setPendingCount(pendingRes.data.length);
            } catch (err) {
                console.error('Failed to fetch alerts', err);
            } finally {
                setLoading(false);
            }
        };

        fetchAlerts();
    }, [user.id]);

    const BadgeCard = ({ title, description, route, buttonText,
        badgeCount, badgeColour = 'danger' }) => (
        <div className="col-md-4 mb-3">
            <div className={`card h-100 shadow-sm position-relative ${badgeCount > 0 ? `border-${badgeColour}` : ''
                }`}>
                {badgeCount > 0 && (
                    <span className={`position-absolute top-0 end-0
                        translate-middle badge rounded-pill
                        bg-${badgeColour} ${badgeColour === 'warning' ? 'text-dark' : ''
                        }`}
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
                    Manage your patients with MediTracker
                </p>

                {/* Unread alerts preview */}
                {unreadNotifications.length > 0 && (
                    <div className="card mb-4 border-danger shadow-sm">
                        <div className="card-header bg-danger text-white
                                        fw-bold d-flex justify-content-between">
                            <span>⚠ Unread Alerts</span>
                            <span className="badge bg-white text-danger">
                                {unreadNotifications.length}
                            </span>
                        </div>
                        <div className="card-body">
                            {unreadNotifications.slice(0, 3).map(n => (
                                <div key={n.id}
                                    className="border-bottom pb-2 mb-2">
                                    <strong>{n.patientName}</strong>
                                    <span className="text-muted ms-2">
                                        {n.vitalType.replace('_', ' ')}
                                    </span>
                                    <span className="ms-2 text-danger fw-bold">
                                        {n.readingValue}
                                    </span>
                                </div>
                            ))}
                            <button
                                className="btn btn-outline-danger btn-sm mt-1"
                                onClick={() => navigate('/notifications')}>
                                View all alerts
                            </button>
                        </div>
                    </div>
                )}

                <h5 className="mb-3 text-muted">Quick Actions</h5>
                <div className="row">
                    <BadgeCard
                        title="Alerts"
                        description="View patient vital reading alerts"
                        route="/notifications"
                        buttonText="View Alerts"
                        badgeCount={unreadNotifications.length}
                        badgeColour="danger"
                    />
                    <BadgeCard
                        title="Appointments"
                        description="Confirm or manage patient appointments"
                        route="/doctor-appointments"
                        buttonText="View Appointments"
                        badgeCount={pendingCount}
                        badgeColour="warning"
                    />
                    <div className="col-md-4 mb-3">
                        <div className="card h-100 shadow-sm">
                            <div className="card-body">
                                <h5 className="card-title">My Patients</h5>
                                <p className="card-text text-muted">
                                    View and search all assigned patients
                                </p>
                                <button
                                    className="btn btn-primary"
                                    onClick={() => navigate('/my-patients')}>
                                    View Patients
                                </button>
                            </div>
                        </div>
                    </div>
                    <div className="col-md-4 mb-3">
                        <div className="card h-100 shadow-sm">
                            <div className="card-body">
                                <h5 className="card-title">
                                    Issue Prescription
                                </h5>
                                <p className="card-text text-muted">
                                    Issue a new prescription for a patient
                                </p>
                                <button
                                    className="btn btn-primary"
                                    onClick={() =>
                                        navigate('/issue-prescription')}>
                                    Issue Prescription
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}