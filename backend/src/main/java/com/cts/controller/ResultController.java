package com.cts.controller;

import com.cts.dto.ExamResultOutputDTO;
import com.cts.service.ExamResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/results")
@Tag(name = "Results", description = "Exam results — accessible even after student account deactivation")
public class ResultController {

    private final ExamResultService examResultService;

    @Operation(summary = "Get exam results for a student",
               description = "Returns published results for the given student. " +
                       "Accessible even after the student account is deactivated. " +
                       "Returns a meaningful message if no results have been published yet.")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<ExamResultOutputDTO>> getResultsByStudent() {
        return new ResponseEntity<>(examResultService.getResultsByStudent(),HttpStatus.OK);
    }
}
