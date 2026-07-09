package com.cts.repository;

import com.cts.entity.CourseMaterialFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseMaterialFileRepository
        extends JpaRepository<CourseMaterialFile, Long> {

    List<CourseMaterialFile> findByCourse_CourseId(Long courseId);
}