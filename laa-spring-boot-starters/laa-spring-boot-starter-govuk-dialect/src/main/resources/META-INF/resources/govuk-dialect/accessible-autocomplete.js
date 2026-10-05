// Enhances govuk:select selects into accessible autocompletes. Load after accessible-autocomplete.
(function () {
  'use strict';

  var SELECTOR = 'select[data-module="accessible-autocomplete"]';

  function optionText(option) {
    return option.textContent || option.innerText;
  }

  function findOption(select, text) {
    return [].filter.call(select.options, function (option) {
      return option.value && optionText(option) === text;
    })[0];
  }

  // Keeps the current option when its text still matches, so duplicate labels keep their value.
  function syncToText(select, text) {
    var current = select.options[select.selectedIndex];
    if (current && current.value && optionText(current) === text) {
      return;
    }
    var match = findOption(select, text);
    setValue(select, match ? match.value : '');
  }

  function escapeHtml(text) {
    var element = document.createElement('div');
    element.textContent = text;
    return element.innerHTML;
  }

  function setValue(select, value) {
    if (select.value !== value) {
      select.value = value;
      select.dispatchEvent(new Event('change', { bubbles: true }));
    }
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

    window.accessibleAutocomplete.enhanceSelectElement({
      selectElement: select,
      showAllValues: select.getAttribute('data-show-all-values') === 'true',
      // Keeps the placeholder text out of the input.
      defaultValue: '',
      // Options are objects so a picked option keeps its value.
      source: function (query, populate) {
        if (!query && empty) {
          populate([empty].concat(options));
          return;
        }
        var lower = query.toLowerCase();
        populate(options.filter(function (option) {
          return option.text.toLowerCase().indexOf(lower) !== -1;
        }));
      },
      templates: {
        inputValue: function (option) {
          return option && option.value ? option.text : '';
        },
        // The library renders suggestions as HTML; option text must stay text.
        suggestion: function (option) {
          return option ? escapeHtml(option.text) : '';
        }
      },
      inputClasses: select.classList.contains('govuk-select--error') ? 'govuk-input--error' : null,
      // A click or Enter takes the option picked; a blur only follows the text.
      onConfirm: function (confirmed) {
        if (confirmed && !confirmed.value) {
          setValue(select, '');
          return;
        }
        if (confirmed && explicit) {
          setValue(select, confirmed.value);
          return;
        }
        var input = document.getElementById(id);
        syncToText(select, confirmed ? confirmed.text : (input ? input.value : ''));
      }
    });

    var container = select.previousElementSibling;
    var markExplicit = function () {
      explicit = true;
      setTimeout(function () { explicit = false; }, 0);
    };
    container.addEventListener('click', function (event) {
      if (event.target.closest('li')) {
        markExplicit();
      }
    }, true);
    container.addEventListener('keydown', function (event) {
      if (event.key === 'Enter' || (event.key === ' ' && event.target.tagName === 'LI')) {
        markExplicit();
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
