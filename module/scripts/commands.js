function identify(metrics) {
  $.post("command/quality-extension/identifyQualityProblems/", {
    "engine" : JSON.stringify(ui.browsingEngine.getJSON()),
    "project" : theProject.id,
    "metrics" : JSON.stringify(metrics)
  }, function(data) {
     window.location.reload(true);
  });
}

// fix later
function transform(metrics) {
  var self = this;
  self._dismissBusy = DialogSystem.showBusy('Identifying quality problems ...');
  $.post("command/quality-extension/transformData/", {
    "engine" : JSON.stringify(ui.browsingEngine.getJSON()),
    "project" : theProject.id
  }, function(data) {
    // fix later
    identify(metrics);
    console.log("success");
    self._dismissBusy();
  });
}
