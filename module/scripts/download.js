function DownloadDialog() {
  var self = this;
  // value goes to html 
  this.serializations = {
           "Turtle": "Turtle",
           "JSON-LD": "JSON-LD",
           "N-TRIPLES": "N-TRIPLES",
           "RDF/XML": "RDF/XML",
           "N3": "N3",
           "JSON-LD": "JSON-LD",
           "RDF/XML-ABBREV": "RDF/XML-ABBREV",
           "RDF/JSON": "RDF/JSON",
   };
}

DownloadDialog.prototype = {

  init : function() {

    this.dialogElement = $(DOM.loadHTML("quality-extension", "scripts/dialogs/download.html"));
    this._elmts = DOM.bind(this.dialogElement);
    self.main_ul = this.dialogElement.find('ul#example');
    self.close = this._elmts.close;

    for (var ser in this.serializations) {
      var value = this.serializations[ser];
      var item = $('<li><input type="checkbox" name="'+ser +'"/>' + value + '</li>');
      self.main_ul.append(item);
    }

    this._elmts.close.click(function() {
      DialogSystem.dismissUntil(self.dialogLevel - 1);
    });

    this._elmts.ok.click(function() {
      var serializations = [];
      self.main_ul.find("input[type='checkbox']").each(function() {
      var name = $(this).attr("name");
      var val = $(this).is(":checked");
        if (val && name) {
          serializations.push(name);
        }
      });
      exportProject(serializations);
      DialogSystem.dismissUntil(self.dialogLevel - 1);
    });

  },

  show : function() {
    this.init();
    self.dialogLevel = DialogSystem.showDialog(this.dialogElement);
  },

};
