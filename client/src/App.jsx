import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import PatientDashboard from './pages/PatientDashboard';
import DoctorDashboard from './pages/DoctorDashboard';
import PatientProfile from './pages/PatientProfile';
import SubmitVitals from './pages/SubmitVitals';
import Notifications from './pages/Notifications';
import Appointments from './pages/Appointments';
import IssuePrescription from './pages/IssuePrescription';
import Prescriptions from './pages/Prescriptions';
import VitalHistory from './pages/VitalHistory';
import MyPatients from './pages/MyPatients';
import DoctorAppointments from './pages/DoctorAppointments';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Login />} />
          <Route
            path="/patient-dashboard"
            element={
              <ProtectedRoute>
                <PatientDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/doctor-dashboard"
            element={
              <ProtectedRoute>
                <DoctorDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/patient-profile/:id"
            element={
              <ProtectedRoute>
                <PatientProfile />
              </ProtectedRoute>
            }
          />
          <Route
            path="/submit-vitals"
            element={
              <ProtectedRoute>
                <SubmitVitals />
              </ProtectedRoute>
            }
          />
          <Route
            path="/notifications"
            element={
              <ProtectedRoute>
                <Notifications />
              </ProtectedRoute>
            }
          />
          <Route
            path="/appointments"
            element={
              <ProtectedRoute>
                <Appointments />
              </ProtectedRoute>
            }
          />
          <Route
            path="/issue-prescription"
            element={
              <ProtectedRoute>
                <IssuePrescription />
              </ProtectedRoute>
            }
          />
          <Route
            path="/prescriptions"
            element={
              <ProtectedRoute>
                <Prescriptions />
              </ProtectedRoute>
            }
          />
          <Route
            path="/vital-history"
            element={
              <ProtectedRoute>
                <VitalHistory />
              </ProtectedRoute>
            }
          />
          <Route
            path="/my-patients"
            element={
              <ProtectedRoute>
                <MyPatients />
              </ProtectedRoute>
            }
          />
          <Route
            path="/doctor-appointments"
            element={
              <ProtectedRoute>
                <DoctorAppointments />
              </ProtectedRoute>
            }
          />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}