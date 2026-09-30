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

    window.accessibleAutocomplete.enhanceSelectElement({
      selectElement: select,
      showAllValues: select.getAttribute('data-show-all-values') === 'true',
      // Keeps the placeholder text out of the input.
      defaultValue: '',
      inputClasses: select.classList.contains('govuk-select--error') ? 'govuk-input--error' : null,
      // Clears the select when the text matches no option, so a stale value isn't submitted.
      onConfirm: function (confirmed) {
        var input = document.getElementById(id);
        var text = confirmed !== undefined ? confirmed : (input ? input.value : '');
        var match = findOption(select, text);
        setValue(select, match ? match.value : '');
      }
    });

    var input = document.getElementById(id);
    if (input) {
      if (select.disabled) {
        input.disabled = true;
      }
      if (describedBy.length) {
        keepDescribedBy(input, describedBy);
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
