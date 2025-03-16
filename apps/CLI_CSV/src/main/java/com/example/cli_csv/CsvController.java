package org.example.cli_csv;

import com.example.libs.service.CsvProcessor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/csv")
public class CsvController {
    private final CsvProcessor csvProcessor;

    public CsvController(CsvProcessor csvProcessor) {
        this.csvProcessor = csvProcessor;
    }

    @PostMapping("/process")
    public String processCsv(@RequestParam String csvPath, @RequestParam String mappingPath, @RequestParam String solrUrl) {
        csvProcessor.processCsv(csvPath);
        return "CSV обработан и данные загружены в Solr.";
    }
}
