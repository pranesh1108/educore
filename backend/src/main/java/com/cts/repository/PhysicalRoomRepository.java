package com.cts.repository;

import com.cts.entity.PhysicalRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface PhysicalRoomRepository extends JpaRepository<PhysicalRoom, Long> {

    boolean existsByRoomNameIgnoreCase(String roomName);


    List<PhysicalRoom> findByStatus(String status);

    @Query("SELECT COUNT(r) > 0 FROM PhysicalRoom r WHERE " +
           "r.roomId = :roomId AND " +
           "r.assignedFrom IS NOT NULL AND " +
           "r.assignedUntil IS NOT NULL AND " +
           "r.assignedFrom < :newEnd AND " +
           "r.assignedUntil > :newStart AND " +
           "(:excludeExamId IS NULL OR r.assignedExamId <> :excludeExamId)")
    boolean hasTimeOverlap(@Param("roomId") Long roomId,
                           @Param("newStart") LocalDateTime newStart,
                           @Param("newEnd") LocalDateTime newEnd,
                           @Param("excludeExamId") Long excludeExamId);
}
