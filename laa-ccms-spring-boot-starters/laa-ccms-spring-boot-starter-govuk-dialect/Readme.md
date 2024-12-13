# Custom Thymeleaf Dialect for GOV.UK Buttons

## Introduction
This project provides a custom Thymeleaf dialect to simplify the creation and customization of GOV.UK-styled buttons. Using this custom dialect, developers can generate button HTML elements with the GOV.UK Design System's standards, reducing repetitive boilerplate code and ensuring consistency.

---

## Installation
To use this custom Thymeleaf dialect, add the following dependency to your `build.gradle` file:

```groovy
implementation 'uk.gov.laa.ccms.springboot:laa-ccms-spring-boot-starter-govuk-dialect'
```

---

## How to use a Custom Dialect?
### 1. **Simplified Syntax**
Writing GOV.UK-styled buttons often involves verbose and repetitive HTML, especially when handling attributes like `class`, `id`, `data-*`, or conditional rendering logic. With this custom dialect, you can declare buttons using clean, concise tags like:

```html
<govuk:button th:text="'Click Me!'" href="'/path'" id="'button-id'" classes="'custom-class'"/>
```

This simplifies templates and improves readability, making it easier for developers to focus on application logic rather than markup details.

### 2. **Dynamic Attribute Processing**
This dialect dynamically processes attributes like `th:*`, resolving them using Thymeleaf's expression language. For example:

```html
<govuk:button th:text="${buttonText}" th:href="${link}"/>
```

This ensures that all attributes, including conditional and computed values, are rendered dynamically at runtime.

---

## Features
- **Anchor and Button Elements:** Supports both `<a>` and `<button>` elements based on the presence of an `href` attribute.
- **Dynamic Class Names:** Automatically includes the default `govuk-button` class and allows additional classes via the `classes` attribute.
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