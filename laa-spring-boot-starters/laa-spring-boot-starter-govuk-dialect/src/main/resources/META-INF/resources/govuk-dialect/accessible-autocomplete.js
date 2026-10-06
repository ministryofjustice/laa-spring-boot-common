// Enhances govuk:select selects into accessible autocompletes. Load after accessible-autocomplete.
(function () {
  'use strict';

  var SELECTOR = 'select[data-module="accessible-autocomplete"]';

  function optionText(option) {
    return option.textContent || option.innerText;
  }

  function normalise(text) {
    return (text || '').trim().toLowerCase();
  }

  // Exact text first, then ignoring case and surrounding spaces.
  function findOption(select, text) {
    var options = [].filter.call(select.options, function (option) {
      return option.value;
    });
    return options.filter(function (option) {
      return optionText(option) === text;
    })[0] || options.filter(function (option) {
      return normalise(optionText(option)) === normalise(text);
    })[0];
  }

  function hasPlaceholder(select) {
    return [].some.call(select.options, function (option) {
      return !option.value;
    });
  }

  function setValue(select, value) {
    if (select.value !== value) {
      select.value = value;
      select.dispatchEvent(new Event('change', { bubbles: true }));
    }
  }

  // Keeps the current option when its text still matches, so duplicate labels keep their value.
  function syncToText(select, text) {
    var current = select.options[select.selectedIndex];
    if (current && current.value && normalise(optionText(current)) === normalise(text)) {
      return;
    }
    var match = findOption(select, text);
    if (match) {
      setValue(select, match.value);
    } else if (hasPlaceholder(select)) {
      setValue(select, '');
    }
  }

  function escapeHtml(text) {
    var element = document.createElement('div');
    element.textContent = text;
    return element.innerHTML;
  }

  // Library re-renders drop the hint and error ids.
  function keepDescribedBy(input, ids) {
    var sync = function () {
      var current = (input.getAttribute('aria-describedby') || '').split(/\s+/).filter(Boolean);
      var missing = ids.filter(function (id) { return current.indexOf(id) === -1; });
      if (missing.length) {
        input.setAttribute('aria-describedby', current.concat(missing).join(' '));
      }
    };
    sync();
    new MutationObserver(sync).observe(input, {
      attributes: true,
      attributeFilter: ['aria-describedby']
    });
  }

  // Exact matches, then prefix matches, then the rest, so autoselect picks the closest option.
  function rank(options, query) {
    var lower = normalise(query);
    var exact = [];
    var prefix = [];
    var rest = [];
    options.forEach(function (option) {
      var text = normalise(option.text);
      if (text === lower) {
        exact.push(option);
      } else if (text.indexOf(lower) === 0) {
        prefix.push(option);
      } else if (text.indexOf(lower) !== -1) {
        rest.push(option);
      }
    });
    return exact.concat(prefix, rest);
  }

  // The library seeds its first render with the saved option's text as a plain string.
  function textOf(option) {
    return typeof option === 'string' ? option : option.text;
  }

  function enhance(select) {
    var id = select.id;
    var describedBy = (select.getAttribute('aria-describedby') || '').split(/\s+/).filter(Boolean);
    var options = [].filter.call(select.options, function (option) {
      return option.value;
    }).map(function (option) {
      return { value: option.value, text: optionText(option) };
    });
    var placeholder = [].filter.call(select.options, function (option) {
      return !option.value;
    })[0];
    // Offered first for an empty query, so clearing the input can leave the select empty.
    var empty = placeholder ? { value: '', text: optionText(placeholder) } : null;
    var explicit = false;
    var cancelled = false;

    var typedText = function () {
      var input = document.getElementById(id);
      return input ? input.value : '';
    };

    window.accessibleAutocomplete.enhanceSelectElement({
      selectElement: select,
      showAllValues: select.getAttribute('data-show-all-values') === 'true',
      // Keeps the placeholder text out of the input.
      defaultValue: '',
      // Options are objects so a picked option keeps its value.
      source: function (query, populate) {
        if (!normalise(query) && empty) {
          populate([empty].concat(options));
          return;
        }
        populate(rank(options, query));
      },
      templates: {
        inputValue: function (option) {
          return option && (typeof option === 'string' || option.value) ? textOf(option) : '';
        },
        // The library renders suggestions as HTML; option text must stay text.
        suggestion: function (option) {
          return option ? escapeHtml(textOf(option)) : '';
        }
      },
      inputClasses: select.classList.contains('govuk-select--error') ? 'govuk-input--error' : null,
      // A click, Enter or arrow-and-Tab takes the option picked; anything else follows the text.
      onConfirm: function (confirmed) {
        if (confirmed && typeof confirmed !== 'string' && explicit && !cancelled) {
          setValue(select, confirmed.value);
          return;
        }
        if (!confirmed || cancelled || typeof confirmed === 'string' || !confirmed.value) {
          syncToText(select, typedText());
          return;
        }
        syncToText(select, confirmed.text);
      }
    });

    var container = select.previousElementSibling;
    var reset = function () {
      setTimeout(function () {
        explicit = false;
        cancelled = false;
      }, 0);
    };
    var isOption = function (target) {
      return target.tagName === 'LI' && target.getAttribute('role') === 'option';
    };
    container.addEventListener('click', function (event) {
      if (event.target.closest('li')) {
        explicit = true;
        reset();
      }
    }, true);
    container.addEventListener('keydown', function (event) {
      if (event.key === 'Escape') {
        cancelled = true;
        reset();
      } else if (event.key === 'Enter' || (event.key === ' ' && isOption(event.target))) {
        explicit = true;
        reset();
      }
    }, true);
    // Tabbing away from a highlighted option picks that option.
    container.addEventListener('blur', function (event) {
      if (isOption(event.target)) {
        explicit = true;
        reset();
      }
    }, true);

    var input = document.getElementById(id);
    if (input) {
      if (select.disabled) {
        input.disabled = true;
      }
      if (describedBy.length) {
        keepDescribedBy(input, describedBy);
      }
      // Enter can submit without a confirm or blur, so sync the typed text first.
      if (select.form) {
        select.form.addEventListener('submit', function () {
          syncToText(select, input.value);
        });
      }
    }
    select.setAttribute('data-autocomplete-enhanced', 'true');
  }

  function init(root) {
    if (!window.accessibleAutocomplete) {
      return;
    }
    (root || document).querySelectorAll(SELECTOR).forEach(function (select) {
      if (select.getAttribute('data-autocomplete-enhanced') !== 'true') {
        enhance(select);
      }
    });
  }

  window.GovUkAccessibleAutocomplete = { init: init };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function () { init(); });
  } else {
    init();
  }
})();
