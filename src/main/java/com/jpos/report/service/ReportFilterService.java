package com.jpos.report.service;

import java.nio.file.Path;
import java.util.Date;

public interface ReportFilterService {
    String addFilters(Date newReportDate, String reportPath);
}
