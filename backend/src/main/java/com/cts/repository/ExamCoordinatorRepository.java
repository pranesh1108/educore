package com.cts.repository;

import com.cts.entity.ExamCoordinator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ExamCoordinatorRepository extends JpaRepository<ExamCoordinator, Long> {

    boolean existsByUser_UserId(Long userId);

    Optional<ExamCoordinator> findByUser_Email(String email);

    Optional<ExamCoordinator> findByUser_UserId(Long userId);
}
