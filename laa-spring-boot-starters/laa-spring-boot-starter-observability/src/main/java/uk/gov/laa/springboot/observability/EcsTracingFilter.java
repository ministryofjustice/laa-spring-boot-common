package uk.gov.laa.springboot.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Observability Filter for MDC.
 */
public class EcsTracingFilter extends OncePerRequestFilter {

  private static final String TRACE_ID = "trace.id";
  private static final String TRANSACTION_ID = "transaction.id";

  private static final String SPRING_TRACE_ID = "traceId";
  private static final String SPRING_SPAN_ID = "spanId";

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

    try {
      String traceId = Optional.ofNullable(request.getHeader(TRACE_ID))
              .or(() -> Optional.ofNullable(MDC.get(SPRING_TRACE_ID)))
              .orElseGet(EcsTracingFilter::generateTraceId);

      String transactionId = Optional.ofNullable(request.getHeader(TRANSACTION_ID))
              .or(() -> Optional.ofNullable(MDC.get(SPRING_SPAN_ID)))
              .orElseGet(EcsTracingFilter::generateTransactionId);

      MDC.put(TRACE_ID, traceId);
      MDC.put(TRANSACTION_ID, transactionId);

      MDC.remove(SPRING_TRACE_ID);
      MDC.remove(SPRING_SPAN_ID);

      filterChain.doFilter(request, response);
    } finally {
      MDC.clear();
    }
  }

  private static String generateTraceId() {
    UUID uuid = UUID.randomUUID();
    return String.format("%016x%016x", uuid.getMostSignificantBits(),
            uuid.getLeastSignificantBits());
  }

  private static String generateTransactionId() {
    UUID uuid = UUID.randomUUID();
    return String.format("%016x", uuid.getLeastSignificantBits());
  }
}