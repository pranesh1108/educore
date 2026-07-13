package com.cts.repository;

import com.cts.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByTitleIgnoreCase(String title);

    List<Course> findByInstructor_InstructorId(Long instructorId);

    Optional<Course> findByCourseIdAndInstructor_InstructorId(Long courseId, Long instructorId);

    int countByInstructor_InstructorIdIsNotNull();

    @Query("SELECT c FROM Course c WHERE " +
            "(:title IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :title, '%')))")
    Page<Course> filterCourses(
            @Param("title") String title,
            @Param("topic") String topic,
            Pageable pageable
    );
}