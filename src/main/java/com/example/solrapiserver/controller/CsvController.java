package com.example.solrapiserver.controller;

import com.example.solrapiserver.service.CsvService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/csv")
@RequiredArgsConstructor
public class CsvController {

    private final CsvService csvService;

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
