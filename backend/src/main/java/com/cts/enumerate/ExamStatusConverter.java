package com.cts.enumerate;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ExamStatusConverter implements AttributeConverter<ExamStatus, String> {

    @Override
    public String convertToDatabaseColumn(ExamStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public ExamStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        
        String statusStr = dbData.trim().toUpperCase();
        
        // Dynamically maps any legacy database status values to ACTIVE in-memory
        if (statusStr.equals("DRAFT") || 
            statusStr.equals("SCHEDULED") || 
            statusStr.equals("PUBLISHED") || 
            statusStr.equals("CANCELLED")) {
            return ExamStatus.ACTIVE;
        }
        
        try {
            return ExamStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            // Safe fallback default to ACTIVE
            return ExamStatus.ACTIVE;
        }
    }
}
