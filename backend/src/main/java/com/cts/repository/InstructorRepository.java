package com.cts.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cts.entity.Instructor;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Long> {

    Optional<Instructor> findByUser_UserId(Long userId);
    
    Optional<Instructor> findByUser_Email(String email);

    boolean existsByUser_UserId(Long userId);
}