# Custom Thymeleaf Dialect

## Introduction

This project provides a custom Thymeleaf dialect for GOV.UK-styled components (buttons, details, selects and the MOJ date picker).
Using this custom dialect, developers can generate button HTML elements with the GOV.UK Design System's standards,
reducing repetitive boilerplate code and ensuring consistency.

---

## Installation

To use this custom Thymeleaf dialect, add the following dependency to your `build.gradle` file:

```groovy
implementation 'uk.gov.justice.service.laa:laa-spring-boot-starter-govuk-dialect'
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
```

### Select (accessible autocomplete) Element Tag Processor

`govuk:select` renders a GOV.UK [select](https://design-system.service.gov.uk/components/select/) with label,
hint and error message, enhanced client-side into the
[accessible autocomplete](https://github.com/alphagov/accessible-autocomplete). Without JavaScript it is a
plain `<select>`.

#### Parameters

| Parameter       | Type                     | Description                                                                 | Default |
|-----------------|--------------------------|-----------------------------------------------------------------------------|---------|
| `field`         | String                   | Binds to a `th:object` property like `th:field` (id, name, value, errors).  |         |
| `id`            | String                   | Select id. Required unless `field` is set.                                  | `field` |
| `name`          | String                   | Submitted name.                                                             | `id`    |
| `label`         | String                   | Label text.                                                                 |         |
| `labelClasses`  | String                   | Extra label classes, e.g. `govuk-label--m`.                                 |         |
| `labelHeading`  | String                   | Wraps the label in a heading (`h1`–`h6`).                                   |         |
| `hint`          | String                   | Hint text.                                                                  |         |
| `errorMessage`  | String                   | Error message.                                                              |         |
| `items`         | Collection, array or Map | Options. Use `th:items`. Map keys are values, map values are labels.        |         |
| `itemValue`     | String                   | Item property or map key for the option value. Defaults to the item.        |         |
| `itemLabel`     | String                   | Item property or map key for the option text. Defaults to the value.        |         |
| `value`         | String                   | Selected value.                                                             |         |
| `placeholder`   | String                   | Text for an empty first option.                                             |         |
| `showAllValues` | Boolean                  | Show all options on click, with a dropdown arrow.                           | `false` |
| `classes`       | String                   | Extra select classes.                                                       |         |
| `disabled`      | Boolean                  | Disables the field.                                                         | `false` |
| `data-*`        | String                   | Passed through to the select.                                               |         |

Booleans can be bare (`showAllValues`), `"true"`/`"false"` or an expression. Explicit `id`, `name`, `value`
and `errorMessage` override `field`. All text is HTML-escaped.

#### Usage

```html
<govuk:select id="court" label="Court" th:items="${courts}" itemValue="code" itemLabel="description"
              placeholder="Please select" th:value="${selectedCourt}" showAllValues/>
```

In a `th:object` form:

```html
<govuk:select field="relationship" th:label="#{client.relationship}" th:items="${relationships}"
              itemValue="code" itemLabel="description" th:placeholder="#{site.select}" showAllValues/>
```

As the page heading:

```html
<govuk:select field="officeId" th:label="#{office.select}" labelHeading="h1" labelClasses="govuk-label--l"
              th:items="${offices}" itemValue="id" itemLabel="name"/>
```

A hand-written `<select>` can opt in with `data-module="accessible-autocomplete"` and optionally
`data-show-all-values="true"`.

#### Enabling the autocomplete

The starter serves `/govuk-dialect/accessible-autocomplete.js` and `/govuk-dialect/accessible-autocomplete.css`.
Load them after the `accessible-autocomplete` library, which the application provides:

```html
<link rel="stylesheet" th:href="@{/assets/accessible-autocomplete.min.css}">
<link rel="stylesheet" th:href="@{/govuk-dialect/accessible-autocomplete.css}">
<script th:src="@{/assets/accessible-autocomplete.min.js}"></script>
<script th:src="@{/govuk-dialect/accessible-autocomplete.js}"></script>
```

- With Spring Security, permit `/govuk-dialect/**`.
- Selects are enhanced on page load. For content added later, call `window.GovUkAccessibleAutocomplete.init(element)`.
- The script keeps the hint and error linked to the input, applies the error style, escapes option text in
  the menu, and fires `change` on the select when its value changes.
- The posted value follows the input: a picked option keeps its value even when labels repeat; on blur, Escape
  or submit the select matches the visible text (ignoring case and surrounding spaces), or empties when nothing
  matches.
- Matches are listed exact first, then by prefix, then by containing the text.
- With a `placeholder`, an empty input offers it first, so clearing the field leaves the select empty.
