package com.example.service;

import com.example.libs.service.MapperService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/mapper")
public class MapperController {

    private final MapperService mapperService;

    public MapperController(MapperService mapperService) {
        this.mapperService = mapperService;
    }

    @GetMapping("/fields")
    public Map<String, String> getAllFields() {
        return mapperService.getAllFields();
    }

    // Получение Solr-поля по названию поля из сущности Book
    @GetMapping("/toSolr")
    public String getSolrField(@RequestParam String jsonFieldName) {
        return mapperService.getSolrFieldName(jsonFieldName);
    }

    // Получение названия поля сущности Book по Solr-полю
    @GetMapping("/toJson")
    public String getJsonField(@RequestParam String solrFieldName) {
        return mapperService.getJsonFieldName(solrFieldName);
    }
}
