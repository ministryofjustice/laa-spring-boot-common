package uk.gov.laa.springboot.observability;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ObservabilityAutoConfigurationTest {

  private final WebApplicationContextRunner contextRunner =
          new WebApplicationContextRunner()
                  .withUserConfiguration(ObservabilityAutoConfiguration.class);

  @Test
  void createsBeansWhenEnabled() {
    contextRunner
            .withPropertyValues(
                    "laa.springboot.starter.observability.enabled=true")
            .run(
                    context -> {
                      assertThat(context).hasSingleBean(ObservabilityAutoConfiguration.class);
                      assertThat(context).hasSingleBean(EcsTracingFilter.class);
                    });
  }

  @Test
  void shouldNotLoadAutoConfigurationWhenDisabled() {
    new WebApplicationContextRunner()
            .withUserConfiguration(ObservabilityAutoConfiguration.class)
            .withPropertyValues(
                    "laa.springboot.starter.observability.enabled=false"
            )
            .run(context -> {
              assertThat(context).doesNotHaveBean(ObservabilityAutoConfiguration.class);
              assertThat(context).doesNotHaveBean(EcsTracingFilter.class);
            });
  }

  @Test
  void filterUsesSpringTraceIdWhenNoHeaderProvided() {
    contextRunner
            .withPropertyValues("laa.springboot.starter.observability.enabled=true")
            .run(context -> {
              EcsTracingFilter filter = context.getBean(EcsTracingFilter.class);

              MockHttpServletRequest request = new MockHttpServletRequest();
              MockHttpServletResponse response = new MockHttpServletResponse();

              String springTraceId = "spring-trace-id";
              String springSpanId = "spring-span-id";
              MDC.put("traceId", springTraceId);
              MDC.put("spanId", springSpanId);

              final String[] traceId = new String[1];
              final String[] transactionId = new String[1];

              MockFilterChain filterChain = new MockFilterChain() {
                @Override
                public void doFilter(jakarta.servlet.ServletRequest request,
                                     jakarta.servlet.ServletResponse response) {
                  traceId[0] = MDC.get("trace.id");
                  transactionId[0] = MDC.get("transaction.id");
                }
              };

              filter.doFilter(request, response, filterChain);

              assertThat(traceId[0]).isEqualTo(springTraceId);
              assertThat(transactionId[0]).isEqualTo(springSpanId);
            });
  }

  @Test
  void filterUsesHeaderValuesWhenProvided() {
    contextRunner
            .withPropertyValues("laa.springboot.starter.observability.enabled=true")
            .run(context -> {
              EcsTracingFilter filter = context.getBean(EcsTracingFilter.class);

              MockHttpServletRequest request = new MockHttpServletRequest();
              request.addHeader("trace.id", "header-trace-id");
              request.addHeader("transaction.id", "header-transaction-id");
              MockHttpServletResponse response = new MockHttpServletResponse();

              final String[] traceId = new String[1];
              final String[] transactionId = new String[1];

              MockFilterChain filterChain = new MockFilterChain() {
                @Override
                public void doFilter(jakarta.servlet.ServletRequest request,
                                     jakarta.servlet.ServletResponse response) {
                  traceId[0] = MDC.get("trace.id");
                  transactionId[0] = MDC.get("transaction.id");
                }
              };

              filter.doFilter(request, response, filterChain);

              assertThat(traceId[0]).isEqualTo("header-trace-id");
              assertThat(transactionId[0]).isEqualTo("header-transaction-id");
            });
  }

  @Test
  void filterGeneratesUuidsWhenNoHeadersOrMdcValues() {
    contextRunner
            .withPropertyValues("laa.springboot.starter.observability.enabled=true")
            .run(context -> {
              EcsTracingFilter filter = context.getBean(EcsTracingFilter.class);

              MockHttpServletRequest request = new MockHttpServletRequest();
              MockHttpServletResponse response = new MockHttpServletResponse();

              final String[] traceId = new String[1];
              final String[] transactionId = new String[1];

              MockFilterChain filterChain = new MockFilterChain() {
                @Override
                public void doFilter(jakarta.servlet.ServletRequest request,
                                     jakarta.servlet.ServletResponse response) {
                  traceId[0] = MDC.get("trace.id");
                  transactionId[0] = MDC.get("transaction.id");
                }
              };

              filter.doFilter(request, response, filterChain);

              assertThat(traceId[0]).hasSize(32);
              assertThat(transactionId[0]).hasSize(16);
            });
  }

  @Test
  void filterRemovesSpringMdcValues() {
    contextRunner
            .withPropertyValues("laa.springboot.starter.observability.enabled=true")
            .run(context -> {
              EcsTracingFilter filter = context.getBean(EcsTracingFilter.class);

              MockHttpServletRequest request = new MockHttpServletRequest();
              MockHttpServletResponse response = new MockHttpServletResponse();

              MDC.put("traceId", "spring-trace-id");
              MDC.put("spanId", "spring-span-id");

              MockFilterChain filterChain = new MockFilterChain() {
                @Override
                public void doFilter(jakarta.servlet.ServletRequest request,
                                     jakarta.servlet.ServletResponse response) {
                  assertThat(MDC.get("traceId")).isNull();
                  assertThat(MDC.get("spanId")).isNull();
                  assertThat(MDC.get("trace.id")).isEqualTo("spring-trace-id");
                  assertThat(MDC.get("transaction.id")).isEqualTo("spring-span-id");
                }
              };

              filter.doFilter(request, response, filterChain);
            });
  }

}