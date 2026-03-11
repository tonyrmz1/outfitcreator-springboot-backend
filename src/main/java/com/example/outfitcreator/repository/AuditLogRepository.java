package com.example.outfitcreator.repository;

import com.example.outfitcreator.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for audit log entries.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserIdAndCreatedAtBetween(String userId, LocalDateTime startDate, LocalDateTime endDate);

    List<AuditLog> findByUserId(String userId);

    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);
}
