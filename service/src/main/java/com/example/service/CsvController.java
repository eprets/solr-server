package com.example.service;


import com.example.libs.service.CsvService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/csv")
public class CsvController {
    private final CsvService csvService;

    public CsvController(CsvService csvService) {
        this.csvService = csvService;
    }

    @PostMapping("/upload")
    public String uploadCsv(@RequestParam("file") MultipartFile file) {
        try {
            csvService.processCsv(file);
            return "CSV uploaded successfully!";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
