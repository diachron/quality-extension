importPackage(com.google.refine.quality.commands);
importPackage(com.google.refine.quality.webservices);

var html = "text/html";
var encoding = "UTF-8";
var ClientSideResourceManager = Packages.com.google.refine.ClientSideResourceManager;
var DiachronWebService = new com.google.refine.quality.webservices.DiachronWebService;
var MetricProcessing = new com.google.refine.quality.webservices.MetricProcessing;
var CreateProject = new com.google.refine.quality.commands.CreateProjectCommand;

function init() {
  
  var RefineServlet = Packages.com.google.refine.RefineServlet;
  
  RefineServlet.registerCommand(module, "exportProject", new ExportProjectCommand());
  RefineServlet.registerCommand(module, "identifyQualityProblems", new IdentifyQualityProblemsCommand());
  RefineServlet.registerCommand(module, "transformData", new TransformDataCommand());
  RefineServlet.registerCommand(module, "getHistory", new HistoryCommand());

  ClientSideResourceManager.addPaths(
      "project/scripts",
      module,
      [
       "scripts/commands.js",
       "scripts/utils/collapsible.js",
       "scripts/metrics/metrics-dialog.js",
       "scripts/download/download-selection.js",
       "scripts/download/download-dialog.js",
       "scripts/statistics/statistics.js",
       "scripts/menu.js",
       ]
  );

  ClientSideResourceManager.addPaths(
    "project/styles",
    module,
    [
     "styles/metrics-dialog.less",
     "styles/commands.less"
    ]
  );
}

/*
 * Function invoked to handle each request in a custom way.
 */
function process(path, request, response) {
  var logger = Packages.org.slf4j.LoggerFactory.getLogger("quality-extension-controller");
  logger.info(path);

  if (path === 'open_in_refine') {
    CreateProject.createProjectInOpenRefine(request, response);

  } else if (path === 'ws') {
    var dataurl =  request.getParameter("download");
    logger.info(dataurl);
    // pass dataurl in a context or use request..
    send(request, response, "webservice.vt", {});
  } else if (path === 'clean') {
    DiachronWebService.clean(request, response);
  } else if (path === 'get_cleaning_suggestions') {
    DiachronWebService.getCleaningSuggestions(request, response);
  }

   if (path == "/" || path == "") {
     send(request, response, "index.vt", {});
   };
}

function send(request, response, template, context) {
  butterfly.sendTextFromTemplate(request, response, context, template, encoding, html);
}
