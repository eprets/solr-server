package com.example.solrapiserver.controller.impl;

import com.example.solrapiserver.service.impl.CsvServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class CsvController {

    private final CsvServiceImpl csvServiceImpl;

    @PostMapping("/csv")
    public ResponseEntity<String> uploadCsv(@RequestParam("file") MultipartFile file) {
        try {
            csvServiceImpl.processCsv(file);
            return ResponseEntity.ok("Файл успешно обработан и загружен в Solr");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка обработки файла: " + e.getMessage());
        }
    }
}
