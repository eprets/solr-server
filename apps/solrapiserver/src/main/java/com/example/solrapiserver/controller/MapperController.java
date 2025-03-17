package com.example.solrapiserver.controller;

import com.example.service.MapperService;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/toSolr")
    public String getSolrField(@RequestParam String jsonFieldName) {
        return mapperService.getSolrFieldName(jsonFieldName);
    }

    @GetMapping("/toJson")
    public String getJsonField(@RequestParam String solrFieldName) {
        return mapperService.getJsonFieldName(solrFieldName);
    }
}
