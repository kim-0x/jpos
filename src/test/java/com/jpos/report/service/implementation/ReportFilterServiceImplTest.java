package com.jpos.report.service.implementation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class ReportFilterServiceImplTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void shouldExtractReportDateAndFilterTokensFromReportDirectory() throws Exception {
        File reportFilterFile = createFile("sale-report__Jun-2026.json", "");
        String reportFilterFilePath = reportFilterFile.getParent();
        ReportFilterServiceImpl service = new ReportFilterServiceImpl();
        String json = service.addFilters(new Date(0), reportFilterFilePath);

        assertEquals("{\"reportDate\":[\"Jun-2026\",\"Dec-1969\"]}", json);
    }

    private File createFile(String fileName, String content) throws Exception {
        File file = temporaryFolder.newFile(fileName);
        Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
        return file;
    }
}