import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import Navbar from '../components/Navbar';
import { useAuth } from '../context/AuthContext';
import { PATIENT_SERVICE_URL } from '../config';

export default function MyPatients() {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [patients, setPatients] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [search, setSearch] = useState('');

    useEffect(() => {
        const fetchPatients = async () => {
            try {
                const response = await axios.get(
                    `${PATIENT_SERVICE_URL}/api/patients`,
                    { params: { doctorId: user.id } }
                );
                setPatients(response.data);
            } catch (err) {
                setError('Failed to load patients');
            } finally {
                setLoading(false);
            }
        };
        fetchPatients();
    }, [user.id]);

    const filtered = patients.filter(p =>
        p.name.toLowerCase().includes(search.toLowerCase()) ||
        p.email.toLowerCase().includes(search.toLowerCase()) ||
        (p.medicalHistory &&
            p.medicalHistory.toLowerCase().includes(search.toLowerCase()))
    );

    return (
        <>
            <Navbar />
            <div className="container mt-4">
                <div className="d-flex justify-content-between
                                align-items-center mb-4">
                    <h2>My Patients</h2>
                    <span className="badge bg-primary fs-6">
                        {patients.length} patients
                    </span>
                </div>

                {/* Search */}
                <div className="mb-4">
                    <input
                        type="text"
                        className="form-control"
                        placeholder="Search by name, email or medical history..."
                        value={search}
                        onChange={e => setSearch(e.target.value)}
                    />
                </div>

                {loading && <p className="text-muted">Loading patients...</p>}
                {error && <div className="alert alert-danger">{error}</div>}

                {!loading && filtered.length === 0 && (
                    <div className="alert alert-info">
                        {search
                            ? `No patients found matching "${search}"`
                            : 'No patients assigned yet.'}
                    </div>
                )}

                <div className="row">
                    {filtered.map(patient => (
                        <div key={patient.id} className="col-md-6 mb-3">
                            <div className="card h-100 shadow-sm">
                                <div className="card-body">
                                    <div className="d-flex justify-content-between
                                                    align-items-start mb-2">
                                        <div>
                                            <h5 className="mb-0">
                                                {patient.name}
                                            </h5>
                                            <small className="text-muted">
                                                DOB: {patient.dateOfBirth}
                                            </small>
                                        </div>
                                        <span className="badge bg-secondary">
                                            #{patient.id}
                                        </span>
                                    </div>
                                    <p className="text-muted small mb-1">
                                        {patient.email}
                                    </p>
                                    {patient.medicalHistory && (
                                        <p className="text-muted small mb-1">
                                            <strong>History:</strong>{' '}
                                            {patient.medicalHistory}
                                        </p>
                                    )}
                                    {patient.allergies &&
                                        patient.allergies !== 'None' && (
                                            <span className="badge bg-warning
                                                         text-dark">
                                                ⚠ Allergies: {patient.allergies}
                                            </span>
                                        )}
                                    <div className="mt-3">
                                        <button
                                            className="btn btn-primary btn-sm"
                                            onClick={() => navigate(
                                                `/patient-profile/${patient.id}`
                                            )}>
                                            View Profile & Vitals
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </>
    );
}