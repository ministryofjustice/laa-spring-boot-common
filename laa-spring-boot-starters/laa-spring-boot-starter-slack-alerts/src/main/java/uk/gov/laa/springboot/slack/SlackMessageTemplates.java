package uk.gov.laa.springboot.slack;

final class SlackMessageTemplates {

  private SlackMessageTemplates() {}

  static final String MESSAGE_TEMPLATE =
      """
      {
          "blocks": [
              {
                  "type": "rich_text",
                  "elements": [
                      {
                          "type": "rich_text_section",
                          "elements": [
                              {
                                  "type": "emoji",
                                  "name": "%s"
                              },
                              {
                                  "type": "text",
                                  "text": "   "
                              },
                              {
                                  "type": "text",
                                  "text": "%s",
                                  "style": {
                                      "bold": true
                                  }
                              }
                          ]
                      }
                  ]
              },
              %s
              {
                  "type": "rich_text",
                  "elements": [
                      {
                          "type": "rich_text_section",
                          "elements": [
                              {
                                  "type": "text",
                                  "text": "Namespace: ",
                                  "style": {
                                      "bold": true
                                  }
                              },
                              {
                                  "type": "text",
                                  "text": "%s"
                              }
                          ]
                      }
                  ]
              },
              {
                  "type": "rich_text",
                  "elements": [
                      {
                          "type": "rich_text_section",
                          "elements": [
                              {
                                  "type": "text",
                                  "text": "Server: ",
                                  "style": {
                                      "bold": true
                                  }
                              },
                              {
                                  "type": "text",
                                  "text": "%s"
                              }
                          ]
                      }
                  ]
              },
              {
                  "type": "rich_text",
                  "elements": [
                      {
                          "type": "rich_text_section",
                          "elements": [
                              {
                                  "type": "text",
                                  "text": "Time: ",
                                  "style": {
                                      "bold": true
                                  }
                              },
                              {
                                  "type": "text",
                                  "text": "%s"
                              }
                          ]
                      }
                  ]
              },
              {
                    "type": "divider"
                }
          ]
      }
      """;

  static final String EXTRA_INFORMATION_TEMPLATE =
      """
              {
                  "type": "rich_text",
                  "elements": [
                      {
                          "type": "rich_text_section",
                          "elements": [
                              {
                                  "type": "text",
                                  "text": "Additional Information : ",
                                  "style": {
                                      "bold": true
                                  }
                              }
                          ]
                      },
                      {
                          "type": "rich_text_preformatted",
                          "elements": [
                              {
                                  "type": "text",
                                  "text": "%s"
                              }
                          ]
                      }
                  ]
              },
      """;
}
