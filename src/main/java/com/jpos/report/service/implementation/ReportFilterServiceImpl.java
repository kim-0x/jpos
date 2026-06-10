package com.jpos.report.service.implementation;

import com.jpos.report.service.ReportFilterService;
import utils.DataSourcePathHelper;
import utils.JsonWriter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReportFilterServiceImpl implements ReportFilterService {
    @Override
    public String addFilters(Date newReportDate, String reportPath) {
        var resolvePath = DataSourcePathHelper.getDefaultFilePath(reportPath);
        try (Stream<Path> stream = Files.list(resolvePath)) {
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{");
            jsonBuilder.append("\"reportDate\":[");
            List<String> reportDateStrings = Stream.concat(stream.filter(Files::isRegularFile)
                                    .map(p -> p.getFileName().toString())
                                    .map(filename -> {
                                        String reportDate = "";
                                        int doubleUnderscoreIndex = filename.indexOf("__");
                                        int dotIndex = filename.indexOf(".");
                                        if (doubleUnderscoreIndex >= 0 && dotIndex > doubleUnderscoreIndex + 2) {
                                            reportDate = filename.substring(doubleUnderscoreIndex + 2, dotIndex);
                                        }
                                        return reportDate;
                                    }),
                            Stream.of(JsonWriter.formatDate(newReportDate))
                    )
                    .filter(r -> !r.isEmpty())
                    .distinct()
                    .toList();

            for (int i = 0; i < reportDateStrings.size(); i++) {
                jsonBuilder.append(JsonWriter.toJsonString(reportDateStrings.get(i)));
                if (i < reportDateStrings.size() - 1) {
                    jsonBuilder.append(",");
                }
            }

            jsonBuilder.append("]");
            jsonBuilder.append("}");
            return jsonBuilder.toString();
        } catch (Exception exception) {
            throw new IllegalArgumentException(exception);
        }
    }
}
