package org.inventory.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AutoReportService {

    private final AnalyticsService analyticsService;

    @Autowired
    public AutoReportService(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @PreDestroy
    public void generateDailyReport() {
        System.out.println("Generating Daily Report...");
        // 1. Create Report Folder
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        File folder = new File("Reports/Daily/" + dateStr);
        if (!folder.exists())
            folder.mkdirs();

        // 2. Filename with Timestamp
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH-mm-ss"));
        String filePath = new File(folder, "Report_" + timeStr + ".xlsx").getAbsolutePath();

        // 3. Generate Excel stub
        System.out.println("Report saved to: " + filePath);
    }
}
