function Stat(data) {
  this.data = data;
  var self = this;
}

Stat.prototype = {

  init : function() {

    this.dialogElement = $(DOM.loadHTML("quality-extension", "scripts/dialogs/stat.html"));
    this._elmts = DOM.bind(this.dialogElement);
    self.dialog = this.dialogElement;
    self.main_ul = this.dialogElement.find('#example');
    self.close = this._elmts.close;

    var ul = $('<label> Number of triples: ' + this.data["number_triples"] + '</label>');
    this.dialogElement.find('#example').append(ul);
    this.dialogElement.find('#example').append('<br/>');

    this.dialogElement.find('#example').append('<label> Considered Quality metrics: </label>');
    self.considered = $("<ul/>");
    $.each(this.data["metrics"], function(key, value) {
      var li = $('<li>' + value + '</li>');
      self.considered.append(li);
    });

    this.dialogElement.find('#example').append(self.considered);
    this.dialogElement.find('#example').append('<br/>');
    
    
    this.dialogElement.find('#example').append('<label> Identified Quality Problems: </label>');
    self.ull = $("<ul/>");
    $.each(this.data["problems_count"], function(key, value) {
      var li = $('<li>' + key + ': ' + value + '</li>');
      self.ull.append(li);
    });
    this.dialogElement.find('#example').append(self.ull);

    this._elmts.close.click(function() {
      DialogSystem.dismissUntil(self.dialogLevel - 1);
    });
  },

  show : function() {
    this.init();
    self.dialogLevel = DialogSystem.showDialog(this.dialogElement);
  },
};
