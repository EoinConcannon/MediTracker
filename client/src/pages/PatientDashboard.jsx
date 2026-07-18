import Navbar from '../components/Navbar';

export default function PatientDashboard() {
    return (
        <>
            <Navbar />
            <div className="container mt-4">
                <h2>Patient Dashboard</h2>
                <p>Welcome to MediTracker.</p>
            </div>
        </>
    );
}