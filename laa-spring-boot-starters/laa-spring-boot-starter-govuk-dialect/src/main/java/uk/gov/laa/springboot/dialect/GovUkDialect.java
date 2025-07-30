package uk.gov.laa.springboot.dialect;

import java.util.Set;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.StandardDialect;


/**
 * Develops a custom GOV.UK dialect.
 */
public class GovUkDialect extends AbstractProcessorDialect {

  private static final String DIALECT_NAME = "GovUKDialect";

  public GovUkDialect() {
    super(DIALECT_NAME, "govuk", StandardDialect.PROCESSOR_PRECEDENCE);
  }

  @Override
  public Set<IProcessor> getProcessors(String dialectPrefix) {
    return Set.of(new ButtonElementTagProcessor(), new DetailsElementTagProcessor());
  }
}