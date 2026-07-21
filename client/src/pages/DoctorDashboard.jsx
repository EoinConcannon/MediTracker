import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';

export default function DoctorDashboard() {
    const navigate = useNavigate();

    return (
        <>
            <Navbar />
            <div className="container mt-4">
                <h2>Doctor Dashboard</h2>
                <p className="text-muted">
                    Welcome to MediTracker.
                </p>
                <button
                    className="btn btn-primary"
                    onClick={() => navigate('/patient-profile/1')}>
                    View Patient 1 Profile
                </button>
            </div>
        </>
    );
}