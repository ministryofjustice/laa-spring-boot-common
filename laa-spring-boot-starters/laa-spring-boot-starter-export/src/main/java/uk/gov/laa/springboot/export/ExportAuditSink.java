package uk.gov.laa.springboot.export;

import uk.gov.laa.springboot.export.model.ExportAuditEvent;

/**
 * Records export audit events.
 */
public interface ExportAuditSink {
  void record(ExportAuditEvent event);
}
