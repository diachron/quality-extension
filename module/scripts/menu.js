ExtensionBar.addExtensionMenu({
  "id" : "diachron",
  "label" : "Diachron Quality",
  submenu : [ {

    "id" : "diachron/improve",
    label : "Identify Quality Problems",
    click : function() {
      var jm = new AboutDialog();
      jm.show();
    }
  }, {}, {
    "id" : "diachron/export",
    label : "About",
    click : function() {
    }
  } ]
});
