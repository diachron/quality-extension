package com.google.refine.quality.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONWriter;

import com.google.refine.commands.Command;
import com.google.refine.history.HistoryEntry;
import com.google.refine.model.Project;
import com.google.refine.quality.utilities.LoadJenaModel;
import com.google.refine.quality.utilities.Utilities;
import com.google.refine.quality.commands.TransformData.EditOneCellProcess;
import com.google.refine.quality.metrics.AbstractQualityMetrics;
import com.google.refine.quality.metrics.EmptyAnnotationValue;
import com.google.refine.quality.metrics.HelloWorldMetrics;
import com.google.refine.quality.metrics.IncompatibleDatatypeRange;
import com.google.refine.quality.metrics.LabelsUsingCapitals;
import com.google.refine.quality.metrics.MalformedDatatypeLiterals;
import com.google.refine.quality.metrics.MisplacedClassesOrProperties;
import com.google.refine.quality.metrics.MisusedOwlDatatypeOrObjectProperties;
import com.google.refine.quality.metrics.OntologyHijacking;
import com.google.refine.quality.metrics.ReportProblems;
import com.google.refine.quality.metrics.UndefinedClasses;
import com.google.refine.quality.metrics.UndefinedProperties;
import com.google.refine.quality.metrics.WhitespaceInAnnotation;
import com.google.refine.util.ParsingUtilities;
import com.google.refine.util.Pool;
import com.hp.hpl.jena.sparql.core.Quad;

public class AssessQuality extends Command{
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Method not implemented");
    }
    
}
