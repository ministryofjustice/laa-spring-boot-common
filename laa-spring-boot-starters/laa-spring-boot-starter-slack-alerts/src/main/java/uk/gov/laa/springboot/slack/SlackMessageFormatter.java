package uk.gov.laa.springboot.slack;

import org.springframework.util.StringUtils;

final class SlackMessageFormatter {

  private SlackMessageFormatter() {}

  static String formatMessage(
      String iconName,
      String headline,
      String extraInfo,
      String namespace,
      String server,
      String timestamp) {

    String extraInfoPlaceholder = buildExtraInfoBlock(extraInfo);
    return String.format(
        SlackMessageTemplates.MESSAGE_TEMPLATE,
        iconName,
        headline,
        extraInfoPlaceholder,
        namespace,
        server,
        timestamp);
  }

  private static String buildExtraInfoBlock(String extraInfo) {
    if (!StringUtils.hasText(extraInfo)) {
      return "";
    }
    return String.format(SlackMessageTemplates.EXTRA_INFORMATION_TEMPLATE, extraInfo);
  }
}
