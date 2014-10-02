function identify(metrics) {
  var self = this;
  self._dismissBusy = DialogSystem.showBusy('Identifying quality problems ...');
  $.post("command/quality-extension/identifyQualityProblems/", {
    "engine" : JSON.stringify(ui.browsingEngine.getJSON()),
    "project" : theProject.id,
    "metrics" : JSON.stringify(metrics)
  }, function(data) {
    console.log("success");
    remove();
    self._dismissBusy();
  });
}

// fix later
function transform(metrics) {
  var self = this;
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

function remove() {
  Refine.postCoreProcess("remove-column", {
    columnName : "Column 1"
  }, null, {
    modelsChanged : true
  }, {
    onDone : function(o) {
    }
  });
}

//remove later when feature for processing metrics independently is done.
// function identifyQualityProblems(metrics) {
// if ("" == getCookie(theProject.id + "IsProcessed")) {
// setCookie(theProject.id + "IsProcessed", "true");
// addNewColumnCommand("identifyQualityProblems", metrics);
// } else {
// alert("Quality is already processed.");
// }
// }

//function setCookie(cname, cvalue) {
//  var d = new Date();
//  d.setTime(d.getTime() + (365 * 24 * 60 * 60 * 1000));
//  var expires = "expires=" + d.toGMTString();
//  document.cookie = cname + "=" + cvalue + "; " + expires;
//}
//
//function getCookie(cname) {
//  var name = cname + "=";
//  var ca = document.cookie.split(';');
//  for (var i = 0; i < ca.length; i++) {
//    var c = ca[i];
//    while (c.charAt(0) == ' ')
//      c = c.substring(1);
//    if (c.indexOf(name) != -1)
//      return c.substring(name.length, c.length);
//  }
//  return "";
//}
