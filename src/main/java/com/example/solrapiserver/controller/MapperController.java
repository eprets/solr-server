package com.example.solrapiserver.controller;

import com.example.solrapiserver.service.MapperService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/mapper")
@RequiredArgsConstructor
public class MapperController {

    private final MapperService mapperService;

    // Получение всех маппингов (имя поля JSON -> имя поля Solr)
    @GetMapping("/fields")
    public Map<String, String> getAllFields() {
        return mapperService.getAllFields();
    }

    // Получение типа поля в Solr для заданного поля JSON
    @GetMapping("/fieldType")
    public String getFieldType(@RequestParam String jsonFieldName) {
        return mapperService.getSolrFieldType(jsonFieldName);
    }
}
