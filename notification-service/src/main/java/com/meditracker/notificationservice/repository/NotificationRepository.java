package com.meditracker.notificationservice.repository;

import com.meditracker.notificationservice.model.Notification;
import com.meditracker.notificationservice.model.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
	List<Notification> findByDoctorId(Long doctorId);

	List<Notification> findByDoctorIdAndStatus(Long doctorId, NotificationStatus status);
}