package br.edu.utfpr;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.opencsv.bean.AbstractBeanField;

public class LocalDateConverter extends AbstractBeanField<String, LocalDate> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    protected LocalDate convert(String value) {
        try {
            return LocalDate.parse(value, FORMATTER); // Convert string to LocalDate
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Invalid date format: " + value, e);
        }
    }
}
