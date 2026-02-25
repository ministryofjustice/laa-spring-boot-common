package uk.gov.laa.springboot.export.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.laa.springboot.export.ExportAuditSink;
import uk.gov.laa.springboot.export.model.ExportAuditEvent;

/**
 * Audit sink that logs export events.
 */
public class LogExportAuditSink implements ExportAuditSink {
  private static final Logger log = LoggerFactory.getLogger(LogExportAuditSink.class);

  @Override
  public void record(ExportAuditEvent event) {
    if (event.isSuccess()) {
      log.info(
          "export_success key={} rows={} maxRows={} durationMs={} startedAt={}",
          event.getExportKey(),
          event.getRows(),
          event.getMaxRows(),
          event.getDurationMs(),
          event.getStartedAt());
    } else {
      log.warn(
          "export_failure key={} rows={} maxRows={} durationMs={} startedAt={} error={}",
          event.getExportKey(),
          event.getRows(),
          event.getMaxRows(),
          event.getDurationMs(),
          event.getStartedAt(),
          event.getError());
    }
  }
}
