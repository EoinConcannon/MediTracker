import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    const handleLogout = () => {
        logout();
        navigate('/');
    };

    const handleHome = () => {
        if (user?.role === 'PATIENT') {
            navigate('/patient-dashboard');
        } else {
            navigate('/doctor-dashboard');
        }
    };

    const isActive = (path) => location.pathname === path;

    return (
        <nav className="navbar navbar-expand navbar-dark bg-primary px-4">
            <span
                className="navbar-brand fw-bold"
                style={{ cursor: 'pointer' }}
                onClick={handleHome}>
                MediTracker
            </span>

            {user && (
                <>
                    <div className="navbar-nav me-auto">
                        {user.role === 'PATIENT' && (
                            <>
                                <span
                                    className={`nav-link ${isActive('/patient-dashboard') ? 'active fw-bold' : ''}`}
                                    style={{ cursor: 'pointer' }}
                                    onClick={() => navigate('/patient-dashboard')}>
                                    Home
                                </span>
                                <span
                                    className={`nav-link ${isActive('/submit-vitals') ? 'active fw-bold' : ''}`}
                                    style={{ cursor: 'pointer' }}
                                    onClick={() => navigate('/submit-vitals')}>
                                    Submit Vitals
                                </span>
                                <span
                                    className={`nav-link ${isActive('/appointments') ? 'active fw-bold' : ''}`}
                                    style={{ cursor: 'pointer' }}
                                    onClick={() => navigate('/appointments')}>
                                    Appointments
                                </span>
                            </>
                        )}
                        {user.role === 'DOCTOR' && (
                            <>
                                <span
                                    className={`nav-link ${isActive('/doctor-dashboard') ? 'active fw-bold' : ''}`}
                                    style={{ cursor: 'pointer' }}
                                    onClick={() => navigate('/doctor-dashboard')}>
                                    Home
                                </span>
                                <span
                                    className={`nav-link ${isActive('/notifications') ? 'active fw-bold' : ''}`}
                                    style={{ cursor: 'pointer' }}
                                    onClick={() => navigate('/notifications')}>
                                    Notifications
                                </span>
                            </>
                        )}
                    </div>

                    <div className="d-flex align-items-center gap-3">
                        <span className="text-white">
                            {user.name}
                        </span>
                        <button
                            className="btn btn-outline-light btn-sm"
                            onClick={handleLogout}>
                            Logout
                        </button>
                    </div>
                </>
            )}
        </nav>
    );
}