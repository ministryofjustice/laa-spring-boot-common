package uk.gov.laa.ccms.springboot.dialect;

import java.util.Set;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.StandardDialect;

/**
 * Develops a custom MoJ dialect.
 */
public class MojCustomDialect extends AbstractProcessorDialect {

  public MojCustomDialect() {
    super("MOJ Custom Dialect", "moj", StandardDialect.PROCESSOR_PRECEDENCE);
  }

  @Override
  public Set<IProcessor> getProcessors(String dialectPrefix) {
    return Set.of(new DatePickerElementTagProcessor());
  }
}
