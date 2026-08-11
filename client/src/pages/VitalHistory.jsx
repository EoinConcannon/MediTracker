import { useEffect, useState } from 'react';
import axios from 'axios';
import Navbar from '../components/Navbar';
import { useAuth } from '../context/AuthContext';
import { VITALS_SERVICE_URL } from '../config';

export default function VitalHistory() {
    const { user } = useAuth();
    const [readings, setReadings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [filter, setFilter] = useState('ALL');

    useEffect(() => {
        const fetchReadings = async () => {
            setLoading(true);
            try {
                const params = { patientId: user.id };
                if (filter !== 'ALL') params.vitalType = filter;

                const response = await axios.get(
                    `${VITALS_SERVICE_URL}/api/vitals`,
                    { params }
                );
                setReadings(response.data);
            } catch (err) {
                setError('Failed to load vital readings');
            } finally {
                setLoading(false);
            }
        };

        fetchReadings();
    }, [user.id, filter]);

    const formatReading = (reading) => {
        if (reading.vitalType === 'BLOOD_PRESSURE') {
            return `${reading.systolic}/${reading.diastolic} mmHg`;
        }
        const units = {
            HEART_RATE: 'BPM',
            GLUCOSE: 'mmol/L',
            TEMPERATURE: '°C',
            SPO2: '%'
        };
        return `${reading.value} ${units[reading.vitalType] || ''}`;
    };

    const formatVitalType = (type) =>
        type.replace(/_/g, ' ')
            .toLowerCase()
            .replace(/\b\w/g, c => c.toUpperCase());

    const vitalTypes = [
        'ALL',
        'BLOOD_PRESSURE',
        'HEART_RATE',
        'GLUCOSE',
        'TEMPERATURE',
        'SPO2'
    ];

    const sortedReadings = [...readings].sort(
        (a, b) => new Date(b.timestamp) - new Date(a.timestamp)
    );

    const alertCount = readings.filter(r => r.alertTriggered).length;

    return (
        <>
            <Navbar />
            <div className="container mt-4">

                <div className="d-flex justify-content-between
                                align-items-center mb-4">
                    <div>
                        <h2 className="mb-0">Vital Reading History</h2>
                        {!loading && readings.length > 0 && (
                            <small className="text-muted">
                                {readings.length} readings —{' '}
                                {alertCount > 0 ? (
                                    <span className="text-danger">
                                        {alertCount} alert
                                        {alertCount > 1 ? 's' : ''} triggered
                                    </span>
                                ) : (
                                    <span className="text-success">
                                        no alerts
                                    </span>
                                )}
                            </small>
                        )}
                    </div>
                </div>

                {/* Vital type filter */}
                <div className="mb-4 d-flex flex-wrap gap-2">
                    {vitalTypes.map(type => (
                        <button
                            key={type}
                            className={`btn btn-sm ${filter === type
                                    ? 'btn-primary'
                                    : 'btn-outline-primary'
                                }`}
                            onClick={() => setFilter(type)}>
                            {type === 'ALL'
                                ? 'All Types'
                                : formatVitalType(type)}
                        </button>
                    ))}
                </div>

                {loading && (
                    <p className="text-muted">Loading readings...</p>
                )}

                {error && (
                    <div className="alert alert-danger">{error}</div>
                )}

                {!loading && sortedReadings.length === 0 && (
                    <div className="alert alert-info">
                        {filter === 'ALL'
                            ? 'No vital readings submitted yet.'
                            : `No ${formatVitalType(filter)} readings found.`}
                    </div>
                )}

                {!loading && sortedReadings.length > 0 && (
                    <div className="card shadow-sm">
                        <div className="card-body p-0">
                            <table className="table table-hover mb-0">
                                <thead className="table-light">
                                    <tr>
                                        <th>Type</th>
                                        <th>Reading</th>
                                        <th>Date & Time</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {sortedReadings.map(reading => (
                                        <tr key={reading.id}>
                                            <td>
                                                {formatVitalType(
                                                    reading.vitalType
                                                )}
                                            </td>
                                            <td className="fw-bold">
                                                {formatReading(reading)}
                                            </td>
                                            <td className="text-muted">
                                                {new Date(reading.timestamp)
                                                    .toLocaleString()}
                                            </td>
                                            <td>
                                                {reading.alertTriggered ? (
                                                    <span className="badge bg-danger">
                                                        ⚠ Alert
                                                    </span>
                                                ) : (
                                                    <span className="badge bg-success">
                                                        Normal
                                                    </span>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                )}
            </div>
        </>
    );
}