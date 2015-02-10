WebServiceView = function() {

  this.metrics = {
    "accuracy" : {
      "name" : "Accuracy",
      "subMetrics" : [ {
        "IncompatibleDatatypeRange" : "Incompatible Datatype Range"
      }, {
        "MalformedDatatypeLiterals" : "Malformed Datatype Literals"
      } ]
    },
    "consistency" : {
      "name" : "Consistency",
      "subMetrics" : [
          {
            "HomogeneousDatatypes" : "Homogeneous Datatypes"
          },
          {
            "MisplacedClassesOrProperties" : "Misplaced Classes Or Properties"
          },
          {
            "MisusedOwlDatatypeOrObjectProperties" : "Misused Owl Datatype Or Object Properties"
          }, {
            "OntologyHijacking" : "Ontology Hijacking"
          }, {
            "UndefinedClasses" : "Undefined Classes"
          }, {
            "UndefinedProperties" : "Undefined Properties"
          } ]
    },
    "understandability" : {
      "name" : "Understandability",
      "subMetrics" : [ {
        "EmptyAnnotationValue" : "Empty Annotation Value"
      }, {
        "LabelsUsingCapitals" : "Labels Using Capitals"
      }, {
        "WhitespaceInAnnotation" : "Whitespace In Annotation"
      } ]
    }
  };
};

WebServiceView.prototype = {

  buildHtml : function() {

    var mainUl = $('<ul/>');

    for ( var metricKey in this.metrics) {

      var metric = this.metrics[metricKey];
      var subMetrics = metric["subMetrics"];
      var item = $('<li><input type="checkbox" value="" id="' + metricKey
          + '" />' + metric["name"] + '</li>');

      var ul = $('<ul/>');

      for (var i = 0; i < subMetrics.length; i++) {
        var subMetric = subMetrics[i];

        var subMetricKey = Object.keys(subMetric)[0];
        var subItem = $('<li><input type="checkbox" value="' + subMetricKey
            + '" id="' + subMetricKey + '" />' + subMetric[subMetricKey]
            + '</li>');

        ul.append(subItem);
      }

      item.append(ul);
      mainUl.append(item);
    }
    return mainUl;
  }
};