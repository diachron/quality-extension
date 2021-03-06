ExtensionBar.addExtensionMenu({
  "id" : "diachron",
  "label" : "Diachron Quality",
  submenu : [ {
    "id" : "diachron/improve",
    label : "Identify Quality Problems",
    click : function() {
      new MetricDialog().show();
    }
  }, {
    "id" : "diachron/transform",
    label : "Transform data",
    click : function() {
      transform();
    }
  }, {}, {
    "id" : "diachron/export",
    label : "Export as RDF",
    click : function() {
      new DownloadDialog().show();
    }
  }, {
    "id" : "diachron/history",
    label : "Cleaning report",
    click : function() {
      getHistory();
    }
  } ]
});
