function Download(data) {
  this.data = data;
  var self = this;
}

Download.prototype = {

  init : function() {
    this.dialogElement = $(DOM.loadHTML("quality-extension", "scripts/dialogs/download-dialog.html"));
    this._elmts = DOM.bind(this.dialogElement);
    self.dialog = this.dialogElement;
    self.main_ul = this.dialogElement.find('ul#example');
    self.close = this._elmts.close;
    self.serializations = new DownloadDialog().serializations;

    $.each(this.data["data"], function(key, value) {
      var ul = $('<a id="' + self.serializations[key] + '" href="#" download="project.'
        + serializations[key] + '">' + key + '</a></br>');
      main_ul.append(ul);
    });

    $.each(this.data["data"], function(key, value) {
      self.dialog.find('ul#example a#' + self.serializations[key]).click(function() {
        this.href = "data:text/plain;charset=UTF-8," + encodeURIComponent(value);
      });
    });

    this._elmts.close.click(function() {
      DialogSystem.dismissUntil(self.dialogLevel - 1);
    });
  },

  show : function() {
    this.init();
    self.dialogLevel = DialogSystem.showDialog(this.dialogElement);
  },
};
