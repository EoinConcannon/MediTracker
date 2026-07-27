import { useEffect, useState } from 'react';
import axios from 'axios';
import Navbar from '../components/Navbar';
import { useAuth } from '../context/AuthContext';
import { NOTIFICATION_SERVICE_URL } from '../config';

export default function Notifications() {
    const { user } = useAuth();
    const [notifications, setNotifications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const fetchNotifications = async () => {
        try {
            const response = await axios.get(
                `${NOTIFICATION_SERVICE_URL}/api/notifications`,
                { params: { doctorId: user.id } }
            );
            setNotifications(response.data);
        } catch (err) {
            setError('Failed to load notifications');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchNotifications();
    }, [user.id]);

    const handleMarkAsRead = async (notificationId) => {
        try {
            await axios.patch(
                `${NOTIFICATION_SERVICE_URL}/api/notifications/${notificationId}/read`
            );
            setNotifications(prev =>
                prev.map(n =>
                    n.id === notificationId ? { ...n, status: 'READ' } : n
                )
            );
        } catch (err) {
            alert('Failed to mark as read');
        }
    };

    const unreadCount = notifications.filter(n => n.status === 'UNREAD').length;

    return (
        <>
            <Navbar />
            <div className="container mt-4">
                <div className="d-flex justify-content-between align-items-center mb-4">
                    <h2>
                        Notifications
                        {unreadCount > 0 && (
                            <span className="badge bg-danger ms-2">
                                {unreadCount} unread
                            </span>
                        )}
                    </h2>
                </div>

                {loading && (
                    <p className="text-muted">Loading notifications...</p>
                )}

                {error && (
                    <div className="alert alert-danger">{error}</div>
                )}

                {!loading && notifications.length === 0 && (
                    <div className="alert alert-info">
                        No notifications yet.
                    </div>
                )}

                {notifications.map(notification => (
                    <div
                        key={notification.id}
                        className={`card mb-3 shadow-sm ${notification.status === 'UNREAD'
                                ? 'border-danger'
                                : 'border-secondary'
                            }`}
                        style={{
                            borderLeftWidth: '4px',
                            borderLeftStyle: 'solid'
                        }}>
                        <div className="card-body">
                            <div className="d-flex justify-content-between
                                            align-items-start">
                                <div>
                                    <h5 className={`card-title mb-1 ${notification.status === 'UNREAD'
                                            ? 'fw-bold'
                                            : 'text-muted'
                                        }`}>
                                        {notification.patientName}
                                    </h5>
                                    <p className="card-text mb-1">
                                        {notification.message}
                                    </p>
                                    <small className="text-muted">
                                        {new Date(notification.timestamp)
                                            .toLocaleString()}
                                    </small>
                                </div>
                                <div className="d-flex flex-column
                                                align-items-end gap-2">
                                    <span className={`badge ${notification.status === 'UNREAD'
                                            ? 'bg-danger'
                                            : 'bg-secondary'
                                        }`}>
                                        {notification.status}
                                    </span>
                                    {notification.status === 'UNREAD' && (
                                        <button
                                            className="btn btn-sm btn-outline-secondary"
                                            onClick={() =>
                                                handleMarkAsRead(notification.id)
                                            }>
                                            Mark as read
                                        </button>
                                    )}
                                </div>
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </>
    );
}