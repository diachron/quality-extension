package com.google.refine.quality.commands;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.refine.ProjectManager;
import com.google.refine.commands.Command;
import com.google.refine.commands.HttpUtilities;
import com.google.refine.importing.ImportingJob;
import com.google.refine.importing.ImportingManager;
import com.google.refine.importing.ImportingUtilities;
import com.google.refine.util.ParsingUtilities;

public class CreateProjectCommand extends Command {

  private final static Logger LOG = LoggerFactory.getLogger(CreateProjectCommand.class);

  public void createProjectInOpenRefine(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    ProjectManager.singleton.setBusy(true);
    try {
      Properties parameters = ParsingUtilities.parseUrlParameters(request);
      ImportingJob job = ImportingManager.createJob();
      JSONObject config = job.getOrCreateDefaultConfig();
      ImportingUtilities.loadDataAndPrepareJob(request, response, parameters, job, config);

      String projectName = "project";
      String format = "text/line-based";

      List<Exception> exceptions = new LinkedList<Exception>();

      long projectId = ImportingUtilities.createProject(job, format,
        getDefaultOptions(projectName), exceptions, true);
      LOG.info(String.format("Project has been created. Id: %s", projectId));

      HttpUtilities.redirect(response, "/project?project=" + projectId);
    } catch (Exception e) {
      respondWithErrorPage(request, response, "Failed to import file", e);
    } finally {
      ProjectManager.singleton.setBusy(false);
    }
  }

  private JSONObject getDefaultOptions(String projectName) throws JSONException {
    JSONObject optionObj = new JSONObject();
    optionObj.append("limit", -1);
    optionObj.append("includeFileSources", false);
    optionObj.append("storeBlankRows", true);
    optionObj.append("encoding", "UTF-8");
    optionObj.append("ignoreLines", -1);
    optionObj.append("linesPerRow", 1);
    optionObj.append("storeBlankCellsAsNulls", true);
    optionObj.append("skipDataLines", -1);
    optionObj.append("projectName", projectName);
    return optionObj;
  }
}
