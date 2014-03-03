/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package np;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;


/**
 *
 * @author siddharth
 */
public class defunctInputCompiler {
    private static List<String> inputFields = Arrays.asList(new String[] {"Init", "Actions", "Goal"});
    private String fileString;
    private Map<String, String> fileSections;
    private Map<String, Integer> landmarks;
    
    
    defunctInputCompiler(String fpath)
            throws java.io.IOException
    {
        fileString = getFileAsString(fpath);
        fileSections = getSections(fileString);
        
    }
    
    
    private static String getFileAsString(String fpath) throws java.io.IOException{
       FileInputStream f = null;
       byte[] mybuf = new byte[(int) new File(fpath).length()];
    
       try{
           f = new FileInputStream(fpath);
           f.read(mybuf);
       } finally {
           if (f != null) f.close();
       }
       
       return new String(mybuf);
    }
    
  
   
    private static Map<String,String> getSections(String input){
        Map<String,String> sections = new HashMap<String, String>();
        
        String[] nameSection;
        
        String[] choppedInput = input.split("/\\*");
        //Returns null strings for beginning and end...
        for (String segment:choppedInput){
            if (segment.isEmpty()){
                continue;
            }
            nameSection = segment.split("\\*/");
            
            if (!inputFields.contains(nameSection[0])){
                System.out.println("Error: unknown input field");
                return null;
            }
            sections.put(nameSection[0], nameSection[1]);
        }
        
        return sections;
    }
 
     
    
    private String[] getAxnStrs(){
        String axnSection = fileSections.get("Actions");
        String axnStrs[] = axnSection.split("\n");
        return axnStrs;
    }
    
      
    private String getAxnName(String axnStr){return axnStr.split(":")[0];}
    private String getPreconStr(String axnStr){return axnStr.split(":")[1];}
    private String getEffectStr(String axnStr){return axnStr.split(":")[2];} 
    private String[] getAsgtStrs(String s){return s.split(", *");}
    
 
    private void setLandmarks(){
        /*
         * Find assignments of the form xi=ci where ci!=0
         * in init, axn precons and goal
         * 
         */
        
        List<Assignment> assignmentObjs = new ArrayList<Assignment>();
        
        for (String sectionName:new String[]{"Init", "Goal"}){
            for (Assignment asgt: getAssignmentObjs(fileSections.get(sectionName))){
                assignmentObjs.add(asgt);
            }
        }
        
        for (String axnStr:getAxnStrs()){
            for (Assignment asgt: getAssignmentObjs(getPreconStr(axnStr))){
                assignmentObjs.add(asgt);
            }
        }
        
        for (Assignment asgt:assignmentObjs){
            if (asgt.getLandmark()!=null){
                landmarks.put(asgt.getVar(),asgt.getVal());
            }
        }
    }

    
    private List<Assignment> getAssignmentObjs(String s){
        List<Assignment> asgtList = new ArrayList<Assignment>();
        for (String asgtStr:getAsgtStrs(s)){
            asgtList.add(new Assignment(asgtStr));
        }
        return asgtList;
    }
    
    
    private Map<String, Integer> getAssignments(String section){

        /*section is a comma separated list of variable assignments*/        
        Map<String, Integer> assignments = new HashMap<String, Integer>();
        String[] assignmentStrs = section.split(", *");
        
        for (String assignment:assignmentStrs){
            assi
        }
        
        
        return assignments;        
    }
    
    
    private static AbstractState getInitState(Map<String, String> sections){
        String initSection = sections.get("Init");
        Map<String, Integer> stateEssentials = new HashMap<String, Integer>();
        
        stateEssentials = getAssignments(initSection);
        
        return new AbstractState(stateEssentials);
    }
    
    
    private static BooleanPrecondition getPreconditions(String preconStr){
        return new BooleanPrecondition(getAssignments(preconStr));
    }
    
    
    private static Map<String, Set<String>> getEffects(String effectStr){
        Map<String, Set<String>> effects = new HashMap<String, Set<String>>();
        
        effects.put("VI", new HashSet<String>());
        effects.put("VD", new HashSet<String>());
        
        for (String effect:effectStr.split(", *")){
            if (effect.contains("++")){
                effects.get("VI").add(effect.replace("++", "").trim());
            }
            else if (effect.contains("--")){
                effects.get("VD").add(effect.replace("--", "").trim());
            }
            else{
                System.out.println("Error: unknown effect "+effect);
                return null;
            }
        }
        
        return effects;
    }
    
 
   
    
    private List<Action> getActions(){
        String preconAxn[], effectStr;
        BooleanPrecondition precon;
        List<Action> actionSet = new ArrayList<Action>();
        Map<String, Set<String>> effects;
        int i = 0;
        for (String axnStr:getActionStrings()){
            if (axnStr.isEmpty()){
                continue;
            }

            /* If the precon or effects refer to a landmarked variable, this action
             * needs to be compiled.
             */
            
            precon = getPreconditions(getPreconStr(axnStr));
            effects = getEffects(getEffectStr(axnStr));
            String name = getAxnName(axnStr);
            
            actionSet.add(new Action(effects.get("VI"), effects.get("VD"), 
                    precon, name));
            i+=1;
        }
        return actionSet;
    }

    
    private  BooleanPrecondition getGoal(Map<String, String> sections){
        return getPreconditions(sections.get("Goal"));
    }

    
    public  Domain getDomainFromFile(String pathName) throws java.io.IOException{
        String str = getFileAsString(pathName);
        Map<String, String> sections = getSections(str);
        
        AbstractState init = getInitState(sections);
        List<Action> axnSet = getActions(sections);
        BooleanPrecondition goalCon = getGoal(sections);
                
        return new Domain(init, axnSet, goalCon);
    }
}
    

