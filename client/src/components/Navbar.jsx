import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/');
    };

    return (
        <nav className="navbar navbar-dark bg-primary px-4">
            <span className="navbar-brand fw-bold">MediTracker</span>
            {user && (
                <div className="d-flex align-items-center gap-3">
                    <span className="text-white">
                        {user.name} — {user.role === 'PATIENT' ? 'Patient' : 'Doctor'}
                    </span>
                    <button
                        className="btn btn-outline-light btn-sm"
                        onClick={handleLogout}>
                        Logout
                    </button>
                </div>
            )}
        </nav>
    );
}