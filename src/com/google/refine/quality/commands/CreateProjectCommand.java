/*

Copyright 2010, Google Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

 * Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above
copyright notice, this list of conditions and the following disclaimer
in the documentation and/or other materials provided with the
distribution.
 * Neither the name of Google Inc. nor the names of its
contributors may be used to endorse or promote products derived from
this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,           
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY           
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */

package com.google.refine.quality.commands;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.refine.ProjectManager;
import com.google.refine.commands.Command;
import com.google.refine.commands.HttpUtilities;
import com.google.refine.importing.ImportingJob;
import com.google.refine.importing.ImportingManager;
import com.google.refine.importing.ImportingUtilities;
import com.google.refine.util.JSONUtilities;
import com.google.refine.util.ParsingUtilities;

public class CreateProjectCommand extends Command {

  final static Logger logger = LoggerFactory.getLogger("create-project_command");

  public void createProjectInOpenRefine(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    ProjectManager.singleton.setBusy(true);
    try {
      Properties parameters = ParsingUtilities.parseUrlParameters(request);
      ImportingJob job = ImportingManager.createJob();
      JSONObject config = job.getOrCreateDefaultConfig();
      ImportingUtilities.loadDataAndPrepareJob(request, response, parameters, job, config);

      // default TODO move to front end
      String format = "text/line-based";
      String projectName = "project-name1";
      JSONObject optionObj = new JSONObject();
      optionObj.append("limit", -1);
      optionObj.append("includeFileSources", false);
      optionObj.append("storeBlankRows", true);
      optionObj.append("encoding", "");
      optionObj.append("ignoreLines", -1);
      optionObj.append("linesPerRow", 1);
      optionObj.append("storeBlankCellsAsNulls", true);
      optionObj.append("skipDataLines", -1);
      adjustLegacyOptions(format, parameters, optionObj);

      if (projectName != null && !projectName.isEmpty()) {
        JSONUtilities.safePut(optionObj, "projectName", projectName);
      }

      List<Exception> exceptions = new LinkedList<Exception>();

      long projectId = ImportingUtilities.createProject(job, format, optionObj, exceptions, true);

      HttpUtilities.redirect(response, "/project?project=" + projectId);
    } catch (Exception e) {
      respondWithErrorPage(request, response, "Failed to import file", e);
    } finally {
      ProjectManager.singleton.setBusy(false);
    }
  }

  static private void adjustLegacyOptions(String format, Properties parameters, JSONObject optionObj) {
    if (",".equals(parameters.getProperty("separator"))) {
      JSONUtilities.safePut(optionObj, "separator", ",");
    } else if ("\\t".equals(parameters.getProperty("separator"))) {
      JSONUtilities.safePut(optionObj, "separator", "\t");
    }

    adjustLegacyIntegerOption(format, parameters, optionObj, "ignore", "ignoreLines");
    adjustLegacyIntegerOption(format, parameters, optionObj, "header-lines", "headerLines");
    adjustLegacyIntegerOption(format, parameters, optionObj, "skip", "skipDataLines");
    adjustLegacyIntegerOption(format, parameters, optionObj, "limit", "limit");

    adjustLegacyBooleanOption(format, parameters, optionObj, "guess-value-type",
        "guessCellValueTypes", false);
    adjustLegacyBooleanOption(format, parameters, optionObj, "ignore-quotes", "processQuotes", true);
  }

  static private void adjustLegacyIntegerOption(String format, Properties parameters,
      JSONObject optionObj, String legacyName, String newName) {

    String s = parameters.getProperty(legacyName);
    if (s != null && !s.isEmpty()) {
      try {
        JSONUtilities.safePut(optionObj, newName, Integer.parseInt(s));
      } catch (NumberFormatException e) {
      }
    }
  }

  static private void adjustLegacyBooleanOption(String format, Properties parameters,
      JSONObject optionObj, String legacyName, String newName, boolean invert) {

    String s = parameters.getProperty(legacyName);
    if (s != null && !s.isEmpty()) {
      JSONUtilities.safePut(optionObj, newName, Boolean.parseBoolean(s));
    }
  }
}
