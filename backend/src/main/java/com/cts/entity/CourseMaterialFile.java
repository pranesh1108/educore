package com.cts.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_material_file")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseMaterialFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long fileId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", referencedColumnName = "course_id", nullable = false)
    private Course course;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "type", length = 10)
    private String type;

    @Column(name = "text_content", columnDefinition = "TEXT")
    private String textContent;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
}
