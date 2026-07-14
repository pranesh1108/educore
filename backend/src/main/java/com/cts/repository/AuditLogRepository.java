package com.cts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cts.entity.AuditLog;

@Repository
<<<<<<< HEAD
public interface AuditLogRepository extends JpaRepository<AuditLog, Integer>{
}

 
=======
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    // Spring Data JPA query derivation for sorting logs from newest to oldest
    java.util.List<AuditLog> findAllByOrderByCreatedAtDesc();
}
>>>>>>> 37751a7 (update the main code)
