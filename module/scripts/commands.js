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
    } else {
      window.location.reload(true);
    }
    console.log("success");
    self._dismissBusy();
  });
}

function exportProject() {
  var self = this;
  self._dismissBusy = DialogSystem.showBusy('Exporting data...');
  $.post("command/quality-extension/exportProject/", {
    "engine" : JSON.stringify(ui.browsingEngine.getJSON()),
    "project" : theProject.id
//    "serializations" : JSON.stringify(metrics)
  }, function(data) {
//    alert( data );
    window.location.reload(true);
    console.log("success");
    self._dismissBusy();
  });
}