package com.example.solrapiserver.controller;

import com.example.solrapiserver.service.CsvService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class CsvController {

    private final CsvService csvService;

    @PostMapping("/csv")
    public ResponseEntity<String> uploadCsv(@RequestParam("file") MultipartFile file) {
        try {
            csvService.processCsv(file);
            return ResponseEntity.ok("Файл успешно обработан и загружен в Solr");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка обработки файла: " + e.getMessage());
        }
    }
}
