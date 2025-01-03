package uk.gov.laa.ccms.springboot.dialect;

import java.util.Set;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;

public class MojCustomDialect extends AbstractProcessorDialect {

  public MojCustomDialect() {
    super("MOJ Custom Dialect", "moj", 1000);
  }

  @Override
  public Set<IProcessor> getProcessors(String dialectPrefix) {
    return Set.of(new DatePickerElementTagProcessor());
  }
}
