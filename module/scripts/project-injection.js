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

// This file is added to the /project page

var SampleExtension = {};

function setCookie(cname, cvalue) {
    var d = new Date();
    d.setTime(d.getTime() + (365*24*60*60*1000));
    var expires = "expires="+d.toGMTString();
    document.cookie = cname + "=" + cvalue + "; " + expires;
}

function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for(var i=0; i<ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0)==' ') c = c.substring(1);
        if (c.indexOf(name) != -1) return c.substring(name.length,c.length);
    }
    return "";
}

function renameColumn24() {
	Refine.postCoreProcess(
	        "rename-column", 
	        {
	          oldColumnName: "Column 2 4",
	          newColumnName: "Comments"
	        },
	        null,
	        { modelsChanged: true },
	        {
	          onDone: function(o) {
	          	console.log("success");
          		self._dismissBusy();
	          }
            }
	        
	      );
}

function renameColumn23() {
	Refine.postCoreProcess(
	        "rename-column", 
	        {
	          oldColumnName: "Column 2 3",
	          newColumnName: "Recommendation"
	        },
	        null,
	        { modelsChanged: true },
	        {
	          onDone: function(o) {
	          	renameColumn24();
	          }
            }
	        
	      );
}

function renameColumn22() {
	Refine.postCoreProcess(
	        "rename-column", 
	        {
	          oldColumnName: "Column 2 2",
	          newColumnName: "Problem Description"
	        },
	        null,
	        { modelsChanged: true },
	        {
	          onDone: function(o) {
	          	renameColumn23();
	          }
            }
	        
	      );
}


function renameColumn21() {
	Refine.postCoreProcess(
	        "rename-column", 
	        {
	          oldColumnName: "Column 2 1",
	          newColumnName: "Problem Type"
	        },
	        null,
	        { modelsChanged: true },
	        {
	          onDone: function(o) {
	          	renameColumn22();
	          }
            }
	        
	      );
}

function splitReportProblemColumn() {

	var mode = "separator";
	var config = {
        columnName: "Column 2",
        mode: mode,
        guessCellType: "false",
        removeOriginalColumn: "true"
      };
	config.separator = "|&SPLITCOLUMN&|";
	config.regex = "false";
	config.maxColumns = 4;
	
	Refine.postCoreProcess(
        "split-column", 
        config,
        null,
        { modelsChanged: true },
        {
	          onDone: function(o) {
          		renameColumn21();  
	          }
        }
      );

}


function splitReportProblemRow() {
Refine.postCoreProcess(
        "split-multi-value-cells", 
        {
          columnName: "Column 2",
          keyColumnName: "Subject",
          separator: "|&SPLITROW&|",
          mode: "plain"
        },
        null,
        { rowsChanged: true },
        {
        	onDone: function(o) {
        		splitReportProblemColumn();
        	}
        }
      );
}


function removeColumn() {
	Refine.postCoreProcess(
	      "remove-column", 
	      {
	        columnName: "Column 1"
	      },
	      null,
	      { modelsChanged: true },
	      {
	          onDone: function(o) {
	          	splitReportProblemRow();
	          }
          }
	    );
}

function renameThirdColumn() {
	Refine.postCoreProcess(
	        "rename-column", 
	        {
	          oldColumnName: "Column 3 3",
	          newColumnName: "Object"
	        },
	        null,
	        { modelsChanged: true },
	        {
	          onDone: function(o) {
	          	removeColumn();
	          }
            }
	        
	      );
}

function renameSecondColumn() {
	Refine.postCoreProcess(
	        "rename-column", 
	        {
	          oldColumnName: "Column 3 2",
	          newColumnName: "Predicate"
	        },
	        null,
	        { modelsChanged: true },
	        {
	          onDone: function(o) {
	          	renameThirdColumn();
	          }
            }
	        
	      );
}


function renameFirstColumn() {
	Refine.postCoreProcess(
	        "rename-column", 
	        {
	          oldColumnName: "Column 3 1",
	          newColumnName: "Subject"
	        },
	        null,
	        { modelsChanged: true },
	        {
	          onDone: function(o) {
	          	renameSecondColumn();
	          }
            }
	        
	      );
}

function splitColumn() {

	var mode = "separator";
	var config = {
        columnName: "Column 3",
        mode: mode,
        guessCellType: "false",
        removeOriginalColumn: "true"
      };
	config.separator = "|&SPLIT&|";
	config.regex = "false";
	config.maxColumns = 3;
	
	Refine.postCoreProcess(
        "split-column", 
        config,
        null,
        { modelsChanged: true },
        {
	          onDone: function(o) {
	          renameFirstColumn();  
	          }
        }
      );

}

function transformDataCommand() {
	
	var self = this;
	//self._dismissBusy = DialogSystem.showBusy('Transforming data ...');
	
	$.post("command/quality-extension/transformData/",
			{
			"engine" : JSON.stringify(ui.browsingEngine.getJSON()),
			"project": theProject.id
			},
			function (data)
			{
			splitColumn();	
			});
}

function addNewColumnCommand2() {
	//Add new columns with blank values
	Refine.postCoreProcess(
        "add-column", 
        {
          baseColumnName: "Column 1", 
          expression: "value.replace(/(?s).*/, \"\")", 
          newColumnName: "Column 3", 
          columnInsertIndex: 1,
          onError: "set-to-blank"
        },
        null,
        { modelsChanged: true },
        {
          onDone: function(o) {
            transformDataCommand();
          }
        }
      );
}

function identifyQualityProblemsCommand() {
	var self = this;
	self._dismissBusy = DialogSystem.showBusy('Identifying quality problems ...');
	// Access the quality of data based on Column 1
	$.post("command/quality-extension/identifyQualityProblems/",
			{
			"engine" : JSON.stringify(ui.browsingEngine.getJSON()),
			"project": theProject.id
			},
			function (data)
			{
			console.log("success");
			self._dismissBusy();
			addNewColumnCommand2();
			});
}

function assessQualityCommand() {
	var self = this;
	self._dismissBusy = DialogSystem.showBusy('Assessing Quality ...');
	// Access the quality of data based on Column 1
	$.post("command/quality-extension/assessQuality/",
			{
			"engine" : JSON.stringify(ui.browsingEngine.getJSON()),
			"project": theProject.id
			},
			function (data)
			{
			console.log("success");
			self._dismissBusy();
			addNewColumnCommand2();
			});
}

function addNewColumnCommand(data) {
	//Add new columns with blank values
	Refine.postCoreProcess(
        "add-column", 
        {
          baseColumnName: "Column 1", 
          expression: "value.replace(/(?s).*/, \"\")", 
          newColumnName: "Column 2", 
          columnInsertIndex: 1,
          onError: "set-to-blank"
        },
        null,
        { modelsChanged: true },
        {
          onDone: function(o) {
            if (data == "accessQuality"){
            	assessQualityCommand();
            }
            else {
            	identifyQualityProblemsCommand();
            }
          }
        }
      );
}

function assessQuality() {
	if ("" == getCookie(theProject.id + "IsProcessed")) {
		setCookie(theProject.id + "IsProcessed" ,"true");
		addNewColumnCommand("accessQuality");
	} else {
		alert("Quality is already processed.");
	}
}

function identifyQualityProblems() {
	if ("" == getCookie(theProject.id + "IsProcessed")) {
		setCookie(theProject.id + "IsProcessed","true");
		addNewColumnCommand("identifyQualityProblems");
	} else {
		alert("Quality is already processed.");
	}		
}

ExtensionBar.addExtensionMenu({
	"id": "diachron",
	"label": "Quality",
	"submenu": [
		 {
			 "id":"diachron/quality",
			        	 label: "Assess Quality",
			        	 click: function(){assessQuality();}
		}
	  ,{},
	  	 {
			 "id":"diachron/improve",
			        	 label: "Identify Quality Problems",
			        	 click: function(){identifyQualityProblems();}
		}
		]
	 });
