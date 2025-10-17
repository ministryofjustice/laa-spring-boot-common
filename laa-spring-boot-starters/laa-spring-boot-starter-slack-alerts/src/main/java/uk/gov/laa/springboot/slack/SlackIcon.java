package uk.gov.laa.springboot.slack;

/**
 * Enum representing emoji codes used in Slack notifications.
 */
public enum SlackIcon {

  /** Indicates a completed task or action. */
  DONE("done", "white_check_mark", "Indicates a completed task or action"),

  /** Celebrates a milestone or success. */
  TADA("tada", "tada", "Celebrates a milestone or success"),

  /** Represents an error or failure. */
  ERROR("error", "x", "Represents an error or failure"),

  /** Represents approval or agreement. */
  THUMBS_UP("thumbs_up", "thumbsup", "Represents approval or agreement"),

  /** Represents a celebration or party. */
  PARTY("party", "party", "Represents a celebration or party"),

  /** Represents a drumroll or anticipation. */
  DRUM("drum", "drum", "Represents a drumroll or anticipation"),

  /** Represents deep sadness or disappointment. */
  BROKEN_HEART("broken_heart", "broken_heart", "Represents deep sadness or disappointment"),

  /** Represents waiting or the passage of time. */
  HOURGLASS("hourglass-timer", "hourglass-timer", "Represents waiting or the passage of time"),

  /** Represents speed, urgency, or quick progress. */
  SONIC_RUN("sonic-run", "sonic-run", "Represents speed, urgency, or quick progress"),

  /** Represents information or a neutral update. */
  INFORMATION("information-source", "information-source", "Represents information");

  private final String name;
  private final String code;
  private final String description;

  SlackIcon(String name, String code, String description) {
    this.name = name;
    this.code = code;
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public String toString() {
    return code;
  }
}
