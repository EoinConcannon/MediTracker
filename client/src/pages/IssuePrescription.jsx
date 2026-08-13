import { useEffect, useState } from 'react';
import axios from 'axios';
import Navbar from '../components/Navbar';
import { useAuth } from '../context/AuthContext';
import { MEDICATION_SERVICE_URL, PATIENT_SERVICE_URL } from '../config';

export default function IssuePrescription() {
    const { user } = useAuth();
    const [patients, setPatients] = useState([]);
    const [submitting, setSubmitting] = useState(false);
    const [result, setResult] = useState(null);
    const [error, setError] = useState('');
    const [form, setForm] = useState({
        patientId: '',
        drugName: '',
        dosage: '',
        frequency: '',
        startDate: '',
        endDate: ''
    });

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
            }
        };
        fetchPatients();
    }, [user.id]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setResult(null);
        setSubmitting(true);

        try {
            const response = await axios.post(
                `${MEDICATION_SERVICE_URL}/api/prescriptions`,
                {
                    ...form,
                    patientId: parseInt(form.patientId),
                    doctorId: user.id,
                    endDate: form.endDate || null
                }
            );
            setResult(response.data);
        } catch (err) {
            if (err.response?.data?.error) {
                setError(err.response.data.error);
            } else if (err.response?.data) {
                setError(JSON.stringify(err.response.data));
            } else {
                setError('Failed to issue prescription');
            }
        } finally {
            setSubmitting(false);
        }
    };

    const resetForm = () => {
        setForm({
            patientId: '',
            drugName: '',
            dosage: '',
            frequency: '',
            startDate: '',
            endDate: ''
        });
        setResult(null);
        setError('');
    };

    return (
        <>
            <Navbar />
            <div className="container mt-4" style={{ maxWidth: '640px' }}>
                <h2 className="mb-4">Issue Prescription</h2>

                {result && (
                    <div className={`alert ${result.allergyWarning
                        ? 'alert-warning' : 'alert-success'} mb-4`}>
                        {result.allergyWarning ? (
                            <>
                                <strong>⚠ Allergy Warning</strong> — this
                                patient has a recorded allergy that may conflict
                                with <strong>{result.drugName}</strong>.
                                The prescription has been saved — please review
                                before proceeding.
                            </>
                        ) : (
                            <>
                                <strong>✓ Prescription issued</strong> —{' '}
                                {result.drugName} {result.dosage},{' '}
                                {result.frequency} has been saved successfully.
                            </>
                        )}
                        <div className="mt-2">
                            <button
                                className="btn btn-sm btn-outline-secondary"
                                onClick={resetForm}>
                                Issue another
                            </button>
                        </div>
                    </div>
                )}

                {!result && (
                    <div className="card shadow-sm p-4">
                        <form onSubmit={handleSubmit}>
                            <div className="mb-3">
                                <label className="form-label">Patient</label>
                                <select
                                    className="form-select"
                                    value={form.patientId}
                                    onChange={e => setForm({
                                        ...form, patientId: e.target.value
                                    })}
                                    required>
                                    <option value="">Select a patient</option>
                                    {patients.map(p => (
                                        <option key={p.id} value={p.id}>
                                            {p.name}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div className="mb-3">
                                <label className="form-label">Drug Name</label>
                                <input
                                    type="text"
                                    className="form-control"
                                    placeholder="e.g. Lisinopril"
                                    value={form.drugName}
                                    onChange={e => setForm({
                                        ...form, drugName: e.target.value
                                    })}
                                    required
                                />
                            </div>

                            <div className="row">
                                <div className="col mb-3">
                                    <label className="form-label">Dosage</label>
                                    <input
                                        type="text"
                                        className="form-control"
                                        placeholder="e.g. 10mg"
                                        value={form.dosage}
                                        onChange={e => setForm({
                                            ...form, dosage: e.target.value
                                        })}
                                        required
                                    />
                                </div>
                                <div className="col mb-3">
                                    <label className="form-label">
                                        Frequency
                                    </label>
                                    <select
                                        className="form-select"
                                        value={form.frequency}
                                        onChange={e => setForm({
                                            ...form, frequency: e.target.value
                                        })}
                                        required>
                                        <option value="">Select</option>
                                        <option>Once daily</option>
                                        <option>Twice daily</option>
                                        <option>Three times daily</option>
                                        <option>As needed</option>
                                        <option>Weekly</option>
                                    </select>
                                </div>
                            </div>

                            <div className="row">
                                <div className="col mb-3">
                                    <label className="form-label">
                                        Start Date
                                    </label>
                                    <input
                                        type="date"
                                        className="form-control"
                                        value={form.startDate}
                                        onChange={e => setForm({
                                            ...form, startDate: e.target.value
                                        })}
                                        required
                                    />
                                </div>
                                <div className="col mb-3">
                                    <label className="form-label">
                                        End Date (optional)
                                    </label>
                                    <input
                                        type="date"
                                        className="form-control"
                                        value={form.endDate}
                                        onChange={e => setForm({
                                            ...form, endDate: e.target.value
                                        })}
                                    />
                                </div>
                            </div>

                            {error && (
                                <div className="alert alert-danger py-2">
                                    {error}
                                </div>
                            )}

                            <button
                                type="submit"
                                className="btn btn-primary w-100"
                                disabled={submitting}>
                                {submitting ? 'Issuing...' : 'Issue Prescription'}
                            </button>
                        </form>
                    </div>
                )}
            </div>
        </>
    );
}