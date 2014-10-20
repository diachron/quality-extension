function identify(metrics) {
  var self = this;
  self._dismissBusy = DialogSystem.showBusy('Identifying quality problems...');
  $.post("command/quality-extension/identifyQualityProblems/", {
    "engine" : JSON.stringify(ui.browsingEngine.getJSON()),
    "project" : theProject.id,
    "metrics" : JSON.stringify(metrics)
  }, function(data) {
    window.location.reload(true);
    console.log("success");
    self._dismissBusy();
  });
}

function transform(metrics) {
  var self = this;
  self._dismissBusy = DialogSystem.showBusy('Tranforming data...');
  $.post("command/quality-extension/transformData/", {
    "engine" : JSON.stringify(ui.browsingEngine.getJSON()),
    "project" : theProject.id
  }, function(data) {
    if (metrics) {
      identify(metrics);
      window.location.reload(true);
    } else {
      window.location.reload(true);
    }
    console.log("success");
    self._dismissBusy();
  });
}

function exportProject(serializations) {
  if (serializations == null) {
    serialization = [];
  }
  var self = this;
  self._dismissBusy = DialogSystem.showBusy('Exporting data...');
  $.post("command/quality-extension/exportProject/", {
    "engine" : JSON.stringify(ui.browsingEngine.getJSON()),
    "project" : theProject.id,
    "serializations" : JSON.stringify(serializations)
  }, function(data) {
    $('<div style="height:100px;width:200px;font-size:13px;"></div>').html(data).dialog();
    console.log("success");
    self._dismissBusy();
  });
}