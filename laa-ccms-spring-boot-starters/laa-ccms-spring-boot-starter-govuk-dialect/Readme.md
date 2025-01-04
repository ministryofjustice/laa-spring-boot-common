# Custom Thymeleaf Dialect

## Introduction

This project provides a custom Thymeleaf dialect to simplify the creation and customization of GOV.UK-styled buttons.
Using this custom dialect, developers can generate button HTML elements with the GOV.UK Design System's standards,
reducing repetitive boilerplate code and ensuring consistency.

---

## Installation

To use this custom Thymeleaf dialect, add the following dependency to your `build.gradle` file:

```groovy
implementation 'uk.gov.laa.ccms.springboot:laa-ccms-spring-boot-starter-govuk-dialect'
```

---

## How to use a Custom Dialect?

### 1. **Simplified Syntax**

Writing GOV.UK-styled buttons often involves verbose and repetitive HTML, especially when handling attributes like
`class`, `id`, `data-*`, or conditional rendering logic. With this custom dialect, you can declare buttons using clean,
concise tags like:

```html

<govuk:button th:text="'Click Me!'" href="'/path'" id="'button-id'" classes="'custom-class'"/>
```

This simplifies templates and improves readability, making it easier for developers to focus on application logic rather
than markup details.

### 2. **Dynamic Attribute Processing**

This dialect dynamically processes attributes like `th:*`, resolving them using Thymeleaf's expression language. For
example:

```html

<govuk:button th:text="${buttonText}" th:href="${link}"/>
```

This ensures that all attributes, including conditional and computed values, are rendered dynamically at runtime.

---

## Features

- **Anchor and Button Elements:** Supports both `<a>` and `<button>` elements based on the presence of an `href`
  attribute.
- **Dynamic Class Names:** Automatically includes the default `govuk-button` class and allows additional classes via the
  `classes` attribute.
- **Accessibility:** Includes `aria-disabled` and other accessibility attributes for disabled buttons.
- **Custom Attributes:** Supports GOV.UK-specific attributes like `data-module` and `data-prevent-double-click`.

---

## Usage

### Prerequisites

- Thymeleaf 3.x
- Spring Boot (for integration)

### Example

#### File-Based Template (test-button.html)

```html
<!DOCTYPE html>
<html xmlns:govuk="http://www.gov.uk">
<body>
<govuk:button th:text="'Click Me!'" href="'/test'" id="'button-id'" classes="'custom-class'"/>
</body>
</html>
```

### Details Element Tag Processor

The `DetailsElementTagProcessor` is a custom Thymeleaf tag processor that enables the use of a `<govuk:details>` tag to
generate a `<details>` HTML element styled with the GOV.UK Design System classes.

#### Features

- Generates a `<details>` element with the `govuk-details` class.
- Includes a `<summary>` element with a customizable summary text.
- Includes a `<div>` element for detailed content.

#### Usage

To use this processor, define a `govuk:details` tag in your Thymeleaf templates and provide the following attributes:

- **`summaryText`**: The text displayed in the summary section of the `<details>` element.
- **`text`**: The content displayed inside the `<div>` when the details are expanded.

#### Example

```html

<govuk:details summaryText="Click to view details" text="This is the detailed content."></govuk:details>
```

### MOJ Date picker Element Tag Processor

The `moj:datepicker` custom tag renders a date picker component using the GOV.UK Design System styles and behavior. This
component is useful for capturing date inputs in a standardized format.

---

### Parameters

| Parameter      | Type   | Description                                                              | Default Value |
|----------------|--------|--------------------------------------------------------------------------|---------------|
| `id`           | String | The unique ID of the input field.                                        | `"date"`      |
| `name`         | String | The name attribute for the input field.                                  | `"date"`      |
| `label`        | String | The label text displayed above the date input.                           | `"Date"`      |
| `hint`         | String | Hint text displayed below the label to guide the user.                   | `""`          |
| `errorMessage` | String | Error message displayed when the input field is invalid.                 | `""`          |
| `minDate`      | String | The minimum date allowed in the date picker (ISO format: `YYYY-MM-DD`).  | `""`          |
| `maxDate`      | String | The maximum date allowed in the date picker (ISO format: `YYYY-MM-DD`).  | `""`          |
| `value`        | String | The pre-filled value of the date input field (ISO format: `YYYY-MM-DD`). | `""`          |

---

### Usage

Add the `moj:datepicker` tag to your Thymeleaf template with the required parameters:

```html
<moj:datepicker 
    id="dob" 
    name="dateOfBirth" 
    label="Date of Birth" 
    hint="For example, 01/01/2000." 
    error="Please enter a valid date of birth." 
    hasError="true" 
    dataMinDate="2000-01-01" 
    dataMaxDate="2025-12-31"
    value="2024-01-01">
</moj:datepicker>

