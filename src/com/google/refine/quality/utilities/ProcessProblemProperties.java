package com.google.refine.quality.utilities;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.List;

/**
 * Retrieve problems messages from file
 * @author Muhammad Ali Qasmi
 *
 */
public class ProcessProblemProperties {
    /**
     * Default file path name
     */
    protected static String filePathName = "extensions/quality-extension/resources/ProblemPropertiesList";
    /**
     * Stores problems list
     */
    protected static Hashtable<String,String> problemPropertiesList = null;
    /**
     * Read problem list in to the hash table
     */
    protected static void readProblemPropertiesFile(){
        try {
            if (null == ProcessProblemProperties.problemPropertiesList){
                ProcessProblemProperties.problemPropertiesList = new Hashtable<String, String>();
            } 
            else {
                ProcessProblemProperties.problemPropertiesList.clear();
            }
            List<String> lines;
            lines = Files.readAllLines(Paths.get(filePathName), Charset.forName("UTF-8"));
            for(String line:lines){
              if (!line.startsWith("#")) {  
                  String[] tmpLine = line.split("=");
                  ProcessProblemProperties.problemPropertiesList.put(tmpLine[0].toLowerCase().trim(), tmpLine[1].trim());
              }
            }
        } catch (IOException e) {
            ProcessProblemProperties.problemPropertiesList.clear();
            ProcessProblemProperties.problemPropertiesList = null;
            e.printStackTrace();
        }
    }
    /**
     * Return problem message w.r.t problem name
     * @param problemName
     * @return
     */
    public static String getProblemMessage(String className) {
        System.out.print("Class Name :" + className);
        if (null != className && !className.isEmpty()){
            if (null == ProcessProblemProperties.problemPropertiesList ){
                readProblemPropertiesFile();
            }
            if (problemPropertiesList.containsKey(className.toLowerCase().trim())){
                System.out.println(" Message : " +  problemPropertiesList.get(className.toLowerCase().trim()));
                return problemPropertiesList.get(className.toLowerCase().trim());
            }
        }
        System.out.println(" Message : " +  " default message.");
        return className.toLowerCase().trim() + " default message.";
    }
}
