import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';
import { PATIENT_SERVICE_URL } from '../config';

export default function Login() {
    const [userId, setUserId] = useState('');
    const [role, setRole] = useState('PATIENT');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        // Validate empty field
        if (!userId.trim()) {
            setError('Please enter your ID');
            return;
        }

        setLoading(true);

        try {
            let response;

            if (role === 'PATIENT') {
                response = await axios.get(
                    `${PATIENT_SERVICE_URL}/api/patients/${userId}`
                );
            } else {
                response = await axios.get(
                    `${PATIENT_SERVICE_URL}/api/doctors/${userId}`
                );
            }

            // Store user in AuthContext
            login({
                id: response.data.id,
                name: response.data.name,
                role: role,
                data: response.data
            });

            // Redirect based on role
            if (role === 'PATIENT') {
                navigate('/patient-dashboard');
            } else {
                navigate('/doctor-dashboard');
            }

        } catch (err) {
            if (err.response && err.response.status === 404) {
                setError('No account found with that ID');
            } else {
                setError('Something went wrong, please try again');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="d-flex justify-content-center 
                        align-items-center vh-100">
            <div className="card shadow p-4" style={{ width: '400px' }}>
                <h3 className="text-center mb-4 text-primary fw-bold">
                    MediTracker
                </h3>
                <form onSubmit={handleSubmit}>

                    <div className="mb-3">
                        <label className="form-label">Role</label>
                        <select
                            className="form-select"
                            value={role}
                            onChange={(e) => setRole(e.target.value)}>
                            <option value="PATIENT">Patient</option>
                            <option value="DOCTOR">Doctor</option>
                        </select>
                    </div>

                    <div className="mb-3">
                        <label className="form-label">
                            {role === 'PATIENT' ? 'Patient ID' : 'Doctor ID'}
                        </label>
                        <input
                            type="number"
                            className="form-control"
                            placeholder={`Enter your ${role === 'PATIENT'
                                ? 'patient' : 'doctor'} ID`}
                            value={userId}
                            onChange={(e) => setUserId(e.target.value)}
                        />
                    </div>

                    {error && (
                        <div className="alert alert-danger py-2">
                            {error}
                        </div>
                    )}

                    <button
                        type="submit"
                        className="btn btn-primary w-100"
                        disabled={loading}>
                        {loading ? 'Logging in...' : 'Login'}
                    </button>

                </form>
            </div>
        </div>
    );
}