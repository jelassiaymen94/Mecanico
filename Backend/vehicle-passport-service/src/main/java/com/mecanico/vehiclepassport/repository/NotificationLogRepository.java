package com.mecanico.vehiclepassport.repository;

import com.mecanico.vehiclepassport.entity.NotificationEventType;
import com.mecanico.vehiclepassport.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    boolean existsByDocumentIdAndEventType(Long documentId, NotificationEventType eventType);

    boolean existsByReminderIdAndEventType(Long reminderId, NotificationEventType eventType);

    List<NotificationLog> findTop50ByOrderByCreatedAtDesc();

    void deleteByVehicleId(Long vehicleId);
}
