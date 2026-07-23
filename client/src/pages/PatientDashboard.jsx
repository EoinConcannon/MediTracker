import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { useAuth } from '../context/AuthContext';

export default function PatientDashboard() {
    const { user } = useAuth();
    const navigate = useNavigate();

    return (
        <>
            <Navbar />
            <div className="container mt-4">
                <h2>Welcome, {user.name}</h2>
                <p className="text-muted">What would you like to do today?</p>

                <div className="row mt-4">
                    <div className="col-md-4 mb-3">
                        <div className="card h-100 shadow-sm">
                            <div className="card-body">
                                <h5 className="card-title">Submit Vitals</h5>
                                <p className="card-text text-muted">
                                    Record your latest health readings
                                </p>
                                <button
                                    className="btn btn-primary"
                                    onClick={() => navigate('/submit-vitals')}>
                                    Submit Reading
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}