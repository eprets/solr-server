package com.example.solrapiserver.controller;

import com.example.solrapiserver.service.FieldsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/fields")
@RequiredArgsConstructor
public class FieldsController {

    private final FieldsService fieldsService;

    @GetMapping
    public Map<String, String> getFields() {
        return fieldsService.getFields();
    }

}
