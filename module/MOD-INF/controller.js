importPackage(com.google.refine.quality.commands);
importPackage(com.google.refine.quality.webservices);

var html = "text/html";
var encoding = "UTF-8";
var ClientSideResourceManager = Packages.com.google.refine.ClientSideResourceManager;
var QualityReport = new com.google.refine.quality.webservices.QualityReport;
var MetricProcessing = new com.google.refine.quality.webservices.MetricProcessing;
var create = new com.google.refine.quality.commands.CreateProjectCommand;

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
       "scripts/collapsible.js",
       "scripts/metrics-dialog.js",
       "scripts/download.js",
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
  var loggerFactory = Packages.org.slf4j.LoggerFactory;
  var logger = loggerFactory.getLogger("quality-extension");
  logger.info(path);

  if (path === 'open_in_refine') {
    create.createProjectInOpenRefine(request, response);
  } else if (path === 'clean') {
    send(request, response, "webservice.vt", {});
//    var html = MetricProcessing.testMetrics(request, response);
//    logger.info(html);
     butterfly.sendString(request, response, html ,"UTF-8", "text/html");
    // in case of error butterfly.sendError(request, response, 404, "unknownservice");
  } else if (path === 'cleaning_suggestions') {
    var json = QualityReport.testMetrics(request, response);
    logger.info(json);
    // TODO
    // handle json or html, what function returns
     butterfly.sendString(request, response, json ,"UTF-8", "text/javascript");
    // in case of error butterfly.sendError(request, response, 404, "unknownservice");
  }

   if (path == "/" || path == "") {
     send(request, response, "index.vt", {});
   };
}

function send(request, response, template, context) {
  butterfly.sendTextFromTemplate(request, response, context, template, encoding, html);
}
