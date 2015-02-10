function MetricDialog() {
  var self = this;

  this.mertics_init = {
    "Representational" : [ {
      "Understandability" : [ { "EmptyAnnotationValue" : "Empty annotation value" },
        { "LabelsUsingCapitals" : "Label using capitals" },
        { "WhitespaceInAnnotation" : "Whitespace in annotation" },
        { "EmptyAnnotationValue" : "EmptyAnnotationValue" } ]
    } ],
    "Intrinsic" : [ {
      "Accuracy" : [ { "IncompatibleDatatypeRange" : "Incompatible datatype range"},
        { "MalformedDatatypeLiterals" : "Malformed datatype literals" } ]
        }, {
      "Consistency" : [ { "MisplacedClassesOrProperties" : "Misplaced classes or properties" },
        { "MisusedOwlDatatypeOrObjectProperties" : "Misused owl datatype or object properties" },
        { "UndefinedClasses" : "Undefined classes" },
        { "UndefinedProperties" : "Undefined properties" },
        { "HomogeneousDatatypes" : "Homogeneous datatypes" },
        { "OntologyHijacking.java" : "Ontology hijacking" } ]
      } ]
  };
}

MetricDialog.prototype = {

  init : function() {

    this.dialogElement = $(DOM.loadHTML("quality-extension", "scripts/dialogs/metrics-dialog.html"));
    this._elmts = DOM.bind(this.dialogElement);
    self.main_ul = this.dialogElement.find('ul#example');
    self.close = this._elmts.close;

    for (var general_metric in this.mertics_init) {
        var sub_metrics = this.mertics_init[general_metric];

        var item = $('<li><input type="checkbox" />' + general_metric + '</li>');
        self.main_ul.append(item);

        var ul = $('<ul/>');
        item.append(ul);
        for (var sub_metric in sub_metrics) {
          var metrics = sub_metrics[sub_metric];
          var name = Object.keys(metrics)[0];
          var metric_arr = metrics[name];

          var sub_item = $('<li><input type="checkbox" />' +  name+ '</li>');
          ul.append(sub_item);
          var ul_ = $('<ul/>');
          sub_item.append(ul_);

          for(var metric in metric_arr) {
            var m = metric_arr[metric];
            var key = Object.keys(m)[0];
            var item = $('<li><input type="checkbox" name="'+key +'"/>' + m[key] + '</li>');
            ul_.append(item);
          }
        }
      }

    jQuery(document).ready(function() {
      self.main_ul.collapsibleCheckboxTree();
    });

    this._elmts.close.click(function() {
      DialogSystem.dismissUntil(self.dialogLevel - 1);
    });

    this._elmts.ok.click(function() {
      var metrics = [];
      self.main_ul.find("input[type='checkbox']").each(function() {
      var name = $(this).attr("name");
      var val = $(this).is(":checked");
        if (val && name) {
          metrics.push(name);
        }
      });
      transform(metrics);
      DialogSystem.dismissUntil(self.dialogLevel - 1);
    });

  },

  show : function() {
    this.init();
    self.dialogLevel = DialogSystem.showDialog(this.dialogElement);
  },

};
