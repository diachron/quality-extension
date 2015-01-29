function changeViewToOpenRefine() {
  $('#btn_submit').attr('value', "Start");
  $('#btn_clean').hide(1);
}

function changeViewToWebService() {
  $('#btn_submit').attr('value', "Cleaning Suggestions");
  $('#btn_clean').show(1);
}
function addMetricesInRequest() {
  var metrices = [];

  $('input[type=checkbox]').each(function() {
    if ($(this).is(":checked") && $(this).val() !== '') {
      metrices.push($(this).val());
    }
  });

  if (metrices.length > 0) {
    $('<input />').attr('type', 'hidden').attr('value',
        JSON.stringify(metrices, null, 2)).attr('id', 'metrics').attr('name',
        'metrics').appendTo('#dataset');
  }
}
$(function() {

  $('#metricsTree').hide(1);
  var webServiceView = new WebServiceView();
  var html = webServiceView.buildHtml();
  $('#metricsTree').append(html);

  changeViewToOpenRefine();

  $('input[type=radio][name=operation]').change(function() {
    if (this.value === 'openRefine') {
      $('#metricsTree').hide(1000);
      changeViewToOpenRefine();
    } else {
      $('#metricsTree').show(1000);
      changeViewToWebService();
    }
  });

  $('#btn_clean').click(function() {
    addMetricesInRequest();
    var url = $('#url_input').val();
    var file = $('#file_input').val();
    var httpMethod = "GET";
    if (url == '' && file != '') {
      httpMethod = "POST";
    } else if (url == '' && file == '') {
      $('#url_input').focus();
    } else if (url != '' && file == '') {
    }
    form.action = "clean_download";
    form.method = httpMethod;
    form.submit();
  });

  $('#btn_submit').click(function() {
    var url = $('#url_input').val();
    var file = $('#file_input').val();

    var httpMethod = "POST";
    if (url == '' && file != '') {
      $('#url_input').attr('name', 'upload');
    } else if (url == '' && file == '') {
      $('#url_input').focus();
    } else if (url != '' && file == '') {
    	httpMethod = "GET";
    }

    if ($("input[name='operation']:checked").val() === 'openRefine') {
      form.action = "open_in_refine";
      form.method = httpMethod;
      form.submit();
    } else {
      addMetricesInRequest();
      form.action = "get_cleaning_suggestions_download";
      form.method = httpMethod;
      form.enctype = "multipart/form-data";
      form.submit();
    }

  });

  $('input[type=checkbox]').on(
      'change',
      function() {
        // one of the parent metrics
        if ($(this).val() === '') {
          $(this).next().find('input[type=checkbox]').prop('checked',
              this.checked);
        } else {
          if (!this.checked) {
            $(this).parent().parent().prev().prop('checked', false);
          } else {
            var siblings = $(this).parent().siblings();
            var allSelected = true;
            for (var i = 0; i < siblings.length; i++) {
              if (!siblings[i].children[0].checked) {
                allSelected = false;
                break;
              }
            }
            $(this).parent().parent().prev().prop('checked', allSelected);
          }
        }
      });
});
