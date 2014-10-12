ExtensionBar.addExtensionMenu({
  "id" : "diachron",
  "label" : "Diachron Quality",
  submenu : [ {
    "id" : "diachron/improve",
    label : "Identify Quality Problems",
    click : function() {
      new AboutDialog().show();
    }
  }, {
    "id" : "diachron/transform",
    label : "Transform data",
    click : function() {
      transform();
    }
  }, {}, {
    "id" : "diachron/export",
    label : "Export",
    click : function() {
      exportProject();
    }
  } ]
});
