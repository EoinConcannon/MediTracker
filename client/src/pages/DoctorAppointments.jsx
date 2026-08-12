import { useEffect, useState } from 'react';
import axios from 'axios';
import Navbar from '../components/Navbar';
import { useAuth } from '../context/AuthContext';
import { APPOINTMENT_SERVICE_URL } from '../config';

export default function DoctorAppointments() {
    const { user } = useAuth();
    const [pending, setPending] = useState([]);
    const [all, setAll] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [filter, setFilter] = useState('PENDING');

    const fetchAppointments = async () => {
        try {
            const [pendingRes, allRes] = await Promise.all([
                axios.get(
                    `${APPOINTMENT_SERVICE_URL}/api/appointments/pending`,
                    { params: { doctorId: user.id } }
                ),
                axios.get(
                    `${APPOINTMENT_SERVICE_URL}/api/appointments`,
                    { params: { doctorId: user.id } }
                )
            ]);
            setPending(pendingRes.data);
            setAll(allRes.data);
        } catch (err) {
            setError('Failed to load appointments');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAppointments();
    }, [user.id]);

    const handleConfirm = async (id) => {
        try {
            await axios.patch(
                `${APPOINTMENT_SERVICE_URL}/api/appointments/${id}/confirm`
            );
            fetchAppointments();
        } catch (err) {
            alert(err.response?.data?.error || 'Failed to confirm appointment');
        }
    };

    const handleCancel = async (id) => {
        if (!window.confirm('Cancel this appointment?')) return;
        try {
            await axios.patch(
                `${APPOINTMENT_SERVICE_URL}/api/appointments/${id}/cancel`
            );
            fetchAppointments();
        } catch (err) {
            alert(err.response?.data?.error || 'Failed to cancel appointment');
        }
    };

    const statusBadge = (status) => {
        const colours = {
            PENDING: 'bg-warning text-dark',
            SCHEDULED: 'bg-success',
            COMPLETED: 'bg-secondary',
            CANCELLED: 'bg-danger',
            NO_SHOW: 'bg-dark'
        };
        return (
            <span className={`badge ${colours[status] || 'bg-secondary'}`}>
                {status}
            </span>
        );
    };

    const displayed = filter === 'PENDING'
        ? pending
        : all.filter(a => filter === 'ALL' || a.status === filter)
            .sort((a, b) => new Date(b.date) - new Date(a.date));

    return (
        <>
            <Navbar />
            <div className="container mt-4">
                <div className="d-flex justify-content-between
                                align-items-center mb-4">
                    <h2>Appointments</h2>
                    {pending.length > 0 && (
                        <span className="badge bg-warning text-dark fs-6">
                            {pending.length} awaiting confirmation
                        </span>
                    )}
                </div>

                {/* Filter tabs */}
                <div className="btn-group mb-4" role="group">
                    {['PENDING', 'SCHEDULED', 'ALL', 'CANCELLED'].map(f => (
                        <button
                            key={f}
                            type="button"
                            className={`btn btn-sm ${filter === f
                                ? 'btn-primary'
                                : 'btn-outline-primary'
                                }`}
                            onClick={() => setFilter(f)}>
                            {f === 'PENDING' ? (
                                <>
                                    Pending
                                    {pending.length > 0 && (
                                        <span className="badge bg-warning
                                                         text-dark ms-1">
                                            {pending.length}
                                        </span>
                                    )}
                                </>
                            ) : f.charAt(0) + f.slice(1).toLowerCase()}
                        </button>
                    ))}
                </div>

                {loading && <p className="text-muted">Loading...</p>}
                {error && <div className="alert alert-danger">{error}</div>}

                {!loading && displayed.length === 0 && (
                    <div className="alert alert-info">
                        {filter === 'PENDING'
                            ? 'No appointments awaiting confirmation.'
                            : `No ${filter.toLowerCase()} appointments.`}
                    </div>
                )}

                {displayed.map(appointment => (
                    <div
                        key={appointment.id}
                        className={`card mb-3 shadow-sm ${appointment.status === 'PENDING'
                            ? 'border-warning'
                            : ''
                            }`}>
                        <div className="card-body">
                            <div className="d-flex justify-content-between
                                            align-items-start">
                                <div>
                                    <h5 className="mb-1">
                                        Patient #{appointment.patientId}
                                    </h5>
                                    <p className="mb-1 text-muted">
                                        {appointment.date} at {appointment.time}
                                    </p>
                                    {appointment.notes && (
                                        <p className="mb-1 text-muted small">
                                            {appointment.notes}
                                        </p>
                                    )}
                                    <div className="mt-1">
                                        {statusBadge(appointment.status)}
                                    </div>
                                </div>
                                {appointment.status === 'PENDING' && (
                                    <div className="d-flex gap-2">
                                        <button
                                            className="btn btn-success btn-sm"
                                            onClick={() =>
                                                handleConfirm(appointment.id)
                                            }>
                                            Confirm
                                        </button>
                                        <button
                                            className="btn btn-outline-danger btn-sm"
                                            onClick={() =>
                                                handleCancel(appointment.id)
                                            }>
                                            Decline
                                        </button>
                                    </div>
                                )}
                                {appointment.status === 'SCHEDULED' && (
                                    <button
                                        className="btn btn-outline-danger btn-sm"
                                        onClick={() =>
                                            handleCancel(appointment.id)
                                        }>
                                        Cancel
                                    </button>
                                )}
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </>
    );
}