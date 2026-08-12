import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import Navbar from '../components/Navbar';
import { useAuth } from '../context/AuthContext';
import { NOTIFICATION_SERVICE_URL, APPOINTMENT_SERVICE_URL } from '../config';

export default function DoctorDashboard() {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [unreadNotifications, setUnreadNotifications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [pendingCount, setPendingCount] = useState(0);

    useEffect(() => {
        const fetchPending = async () => {
            try {
                const response = await axios.get(
                    `${APPOINTMENT_SERVICE_URL}/api/appointments/pending`,
                    { params: { doctorId: user.id } }
                );
                setPendingCount(response.data.length);
            } catch (err) {
                console.error('Failed to fetch pending appointments', err);
            }
        };
        fetchPending();
    }, [user.id]);

    useEffect(() => {
        const fetchUnread = async () => {
            try {
                const response = await axios.get(
                    `${NOTIFICATION_SERVICE_URL}/api/notifications/unread`,
                    { params: { doctorId: user.id } }
                );
                setUnreadNotifications(response.data);
            } catch (err) {
                console.error('Failed to fetch notifications', err);
            } finally {
                setLoading(false);
            }
        };

        fetchUnread();
    }, [user.id]);

    return (
        <>
            <Navbar />
            <div className="container mt-4">
                <h2 className="mb-4">Welcome, {user.name}</h2>

                {/* Unread alerts section */}
                <div className="card mb-4 shadow-sm">
                    <div className="card-header bg-danger text-white fw-bold">
                        ⚠ Unread Alerts
                        {unreadNotifications.length > 0 && (
                            <span className="badge bg-white text-danger ms-2">
                                {unreadNotifications.length}
                            </span>
                        )}
                    </div>
                    <div className="card-body">
                        {loading && (
                            <p className="text-muted mb-0">Loading...</p>
                        )}
                        {!loading && unreadNotifications.length === 0 && (
                            <p className="text-muted mb-0">
                                No unread alerts.
                            </p>
                        )}
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
                                <small className="text-muted ms-2">
                                    {new Date(n.timestamp).toLocaleString()}
                                </small>
                            </div>
                        ))}
                        <button
                            className="btn btn-outline-danger btn-sm mt-2"
                            onClick={() => navigate('/notifications')}>
                            View all notifications
                        </button>
                    </div>
                </div>

                {/* Quick actions */}
                <div className="row">
                    <div className="col-md-4 mb-3">
                        <div className="card h-100 shadow-sm">
                            <div className="card-body">
                                <h5 className="card-title">My Patients</h5>
                                <p className="card-text text-muted">
                                    View all assigned patients
                                </p>
                                <button
                                    className="btn btn-primary"
                                    onClick={() =>
                                        navigate('/patient-profile/1')}>
                                    View Patients
                                </button>
                            </div>
                        </div>
                    </div>
                    <div className="col-md-4 mb-3">
                        <div className="card h-100 shadow-sm">
                            <div className="card-body">
                                <h5 className="card-title">Issue Prescription</h5>
                                <p className="card-text text-muted">
                                    Issue a new prescription for a patient
                                </p>
                                <button
                                    className="btn btn-primary"
                                    onClick={() => navigate('/issue-prescription')}>
                                    Issue Prescription
                                </button>
                            </div>
                        </div>
                    </div>
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
                                    Appointments
                                    {pendingCount > 0 && (
                                        <span className="badge bg-warning text-dark ms-2">
                                            {pendingCount}
                                        </span>
                                    )}
                                </h5>
                                <p className="card-text text-muted">
                                    Confirm or manage patient appointments
                                </p>
                                <button
                                    className="btn btn-primary"
                                    onClick={() => navigate('/doctor-appointments')}>
                                    View Appointments
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}