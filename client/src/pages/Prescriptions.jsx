import { useEffect, useState } from 'react';
import axios from 'axios';
import Navbar from '../components/Navbar';
import { useAuth } from '../context/AuthContext';
import { MEDICATION_SERVICE_URL } from '../config';
import { formatDate } from '../utils/dateUtils';

export default function Prescriptions() {
    const { user } = useAuth();
    const [prescriptions, setPrescriptions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [filter, setFilter] = useState('ACTIVE');

    useEffect(() => {
        const fetchPrescriptions = async () => {
            try {
                const params = { patientId: user.id };
                if (filter !== 'ALL') params.status = filter;

                const response = await axios.get(
                    `${MEDICATION_SERVICE_URL}/api/prescriptions`,
                    { params }
                );
                setPrescriptions(response.data);
            } catch (err) {
                setError('Failed to load prescriptions');
            } finally {
                setLoading(false);
            }
        };

        fetchPrescriptions();
    }, [user.id, filter]);

    const statusBadge = (status) => {
        return (
            <span className={`badge ${status === 'ACTIVE' ? 'bg-success' : 'bg-secondary'
                }`}>
                {status}
            </span>
        );
    };

    return (
        <>
            <Navbar />
            <div className="container mt-4">
                <div className="d-flex justify-content-between
                                align-items-center mb-4">
                    <h2>My Prescriptions</h2>
                    <div className="btn-group" role="group">
                        {['ACTIVE', 'ALL'].map(f => (
                            <button
                                key={f}
                                type="button"
                                className={`btn btn-sm ${filter === f
                                    ? 'btn-primary'
                                    : 'btn-outline-primary'
                                    }`}
                                onClick={() => {
                                    setLoading(true);
                                    setFilter(f);
                                }}>
                                {f === 'ACTIVE' ? 'Active' : 'All'}
                            </button>
                        ))}
                    </div>
                </div>

                {loading && (
                    <p className="text-muted">Loading prescriptions...</p>
                )}

                {error && (
                    <div className="alert alert-danger">{error}</div>
                )}

                {!loading && prescriptions.length === 0 && (
                    <div className="alert alert-info">
                        {filter === 'ACTIVE'
                            ? 'No active prescriptions.'
                            : 'No prescriptions on record.'}
                    </div>
                )}

                {prescriptions.map(prescription => (
                    <div key={prescription.id} className="card mb-3 shadow-sm">
                        <div className="card-body">
                            <div className="d-flex justify-content-between
                                            align-items-start">
                                <div>
                                    <h5 className="mb-1 fw-bold">
                                        {prescription.drugName}
                                    </h5>
                                    <p className="mb-1 text-muted">
                                        {prescription.dosage} —{' '}
                                        {prescription.frequency}
                                    </p>
                                    <small className="text-muted">
                                        Started: {formatDate(prescription.startDate)}
                                        {prescription.endDate && (
                                            <> · Ends: {formatDate(prescription.endDate)}</>
                                        )}
                                    </small>
                                    {prescription.allergyWarning && (
                                        <div className="mt-2">
                                            <span className="badge bg-warning
                                                             text-dark">
                                                ⚠ Allergy Warning
                                            </span>
                                        </div>
                                    )}
                                </div>
                                <div>
                                    {statusBadge(prescription.status)}
                                </div>
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </>
    );
}