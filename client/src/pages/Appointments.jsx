import { useEffect, useState } from 'react';
import axios from 'axios';
import Navbar from '../components/Navbar';
import { useAuth } from '../context/AuthContext';
import { APPOINTMENT_SERVICE_URL } from '../config';

export default function Appointments() {
    const { user } = useAuth();
    const [appointments, setAppointments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showForm, setShowForm] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [formError, setFormError] = useState('');
    const [filter, setFilter] = useState('ALL');
    const [form, setForm] = useState({
        date: '',
        time: '',
        notes: ''
    });

    const fetchAppointments = async () => {
        try {
            const response = await axios.get(
                `${APPOINTMENT_SERVICE_URL}/api/appointments`,
                { params: { patientId: user.id } }
            );
            setAppointments(response.data);
        } catch (err) {
            setError('Failed to load appointments');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAppointments();
    }, [user.id]);

    const handleBook = async (e) => {
        e.preventDefault();
        setFormError('');
        setSubmitting(true);

        try {
            await axios.post(
                `${APPOINTMENT_SERVICE_URL}/api/appointments`,
                {
                    patientId: user.id,
                    doctorId: user.data.assignedDoctorId,
                    date: form.date,
                    time: form.time + ':00',
                    notes: form.notes
                }
            );
            setShowForm(false);
            setForm({ date: '', time: '', notes: '' });
            fetchAppointments();
        } catch (err) {
            if (err.response?.data?.error) {
                setFormError(err.response.data.error);
            } else {
                setFormError('Failed to book appointment');
            }
        } finally {
            setSubmitting(false);
        }
    };

    const handleCancel = async (id) => {
        if (!window.confirm(
            'Are you sure you want to cancel this appointment?'
        )) return;

        try {
            await axios.patch(
                `${APPOINTMENT_SERVICE_URL}/api/appointments/${id}/cancel`
            );
            setAppointments(prev =>
                prev.map(a =>
                    a.id === id ? { ...a, status: 'CANCELLED' } : a
                )
            );
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
            NO_SHOW: 'bg-warning'
        };
        return (
            <span className={`badge ${colours[status] || 'bg-secondary'}`}>
                {status}
            </span>
        );
    };

    const today = new Date().toISOString().split('T')[0];

    const filteredAppointments = appointments
        .filter(a => {
            if (filter === 'UPCOMING') {
                return a.status === 'SCHEDULED' && a.date >= today;
            }
            if (filter === 'SCHEDULED') {
                return a.status === 'SCHEDULED';
            }
            if (filter === 'CANCELLED') {
                return a.status === 'CANCELLED';
            }
            return true;
        })
        .sort((a, b) => {
            if (filter === 'UPCOMING') {
                return new Date(a.date) - new Date(b.date);
            }
            return new Date(b.date) - new Date(a.date);
        });

    return (
        <>
            <Navbar />
            <div className="container mt-4">

                {/* Header */}
                <div className="d-flex justify-content-between
                                align-items-center mb-4">
                    <h2>Appointments</h2>
                    <button
                        className="btn btn-primary"
                        onClick={() => {
                            setShowForm(!showForm);
                            setFormError('');
                        }}>
                        {showForm ? 'Cancel' : '+ Book Appointment'}
                    </button>
                </div>

                {/* Booking form */}
                {showForm && (
                    <div className="card mb-4 shadow-sm">
                        <div className="card-body">
                            <h5 className="card-title mb-3">
                                Book New Appointment
                            </h5>
                            <form onSubmit={handleBook}>
                                <div className="row">
                                    <div className="col-md-6 mb-3">
                                        <label className="form-label">
                                            Date
                                        </label>
                                        <input
                                            type="date"
                                            className="form-control"
                                            value={form.date}
                                            min={today}
                                            onChange={e => setForm({
                                                ...form,
                                                date: e.target.value
                                            })}
                                            required
                                        />
                                    </div>
                                    <div className="col-md-6 mb-3">
                                        <label className="form-label">
                                            Time
                                        </label>
                                        <input
                                            type="time"
                                            className="form-control"
                                            value={form.time}
                                            onChange={e => setForm({
                                                ...form,
                                                time: e.target.value
                                            })}
                                            required
                                        />
                                    </div>
                                </div>
                                <div className="mb-3">
                                    <label className="form-label">
                                        Notes (optional)
                                    </label>
                                    <textarea
                                        className="form-control"
                                        rows="2"
                                        value={form.notes}
                                        onChange={e => setForm({
                                            ...form,
                                            notes: e.target.value
                                        })}
                                    />
                                </div>
                                {formError && (
                                    <div className="alert alert-danger py-2">
                                        {formError}
                                    </div>
                                )}
                                <button
                                    type="submit"
                                    className="btn btn-primary"
                                    disabled={submitting}>
                                    {submitting
                                        ? 'Booking...'
                                        : 'Confirm Booking'}
                                </button>
                            </form>
                        </div>
                    </div>
                )}

                {/* Filter tabs */}
                {!loading && appointments.length > 0 && (
                    <div className="btn-group mb-4" role="group">
                        {['ALL', 'UPCOMING', 'SCHEDULED', 'CANCELLED'].map(
                            f => (
                                <button
                                    key={f}
                                    type="button"
                                    className={`btn btn-sm ${filter === f
                                        ? 'btn-primary'
                                        : 'btn-outline-primary'
                                        }`}
                                    onClick={() => setFilter(f)}>
                                    {f === 'ALL'
                                        ? 'All'
                                        : f === 'UPCOMING'
                                            ? 'Upcoming'
                                            : f === 'SCHEDULED'
                                                ? 'Scheduled'
                                                : 'Cancelled'}
                                </button>
                            )
                        )}
                    </div>
                )}

                {loading && (
                    <p className="text-muted">Loading appointments...</p>
                )}
                {error && (
                    <div className="alert alert-danger">{error}</div>
                )}

                {!loading && filteredAppointments.length === 0 && (
                    <div className="alert alert-info">
                        {filter === 'ALL'
                            ? 'No appointments yet. Book your first one above.'
                            : `No ${filter.toLowerCase()} appointments found.`}
                    </div>
                )}

                {filteredAppointments.map(appointment => (
                    <div
                        key={appointment.id}
                        className="card mb-3 shadow-sm">
                        <div className="card-body">
                            <div className="d-flex justify-content-between
                                            align-items-start">
                                <div>
                                    <h5 className="mb-1">
                                        {appointment.date} at {appointment.time}
                                    </h5>
                                    {appointment.notes && (
                                        <p className="text-muted mb-1">
                                            {appointment.notes}
                                        </p>
                                    )}
                                    <div className="mt-1">
                                        {statusBadge(appointment.status)}
                                    </div>
                                </div>
                                {(appointment.status === 'SCHEDULED' ||
                                    appointment.status === 'PENDING') && (
                                        <button
                                            className="btn btn-outline-danger btn-sm"
                                            onClick={() => handleCancel(appointment.id)}>
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