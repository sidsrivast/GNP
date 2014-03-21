/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package np;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author siddharth
 */
public class NPinput {
    private static List<String> inputFields = Arrays.asList(new String[] {"Init", "Actions", "Goal"});
    private static LandmarkBunch landmarks = new LandmarkBunch();
    public static String inputFile = new String();
    public static LandmarkBunch getLandmarks() {return landmarks;}
    private static Domain inputDomain;
        

    NPinput(String fpath) throws java.io.IOException{
        inputFile = fpath;
        inputDomain = getDomainFromFile(inputFile);
        
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
    
    
    public static Domain getDomainFromFile(String pathName) throws java.io.IOException{
        /* Main entry point for reading the input file.
         * Different parsing methods use setLandmarks method
         * to set  as they are found.
         */
        
        String str = getFileAsString(pathName);
        Map<String, String> sections = getSections(str);
        
        /* first parse to get all assignments */
        ConcreteState concreteInit = getConcreteInitState(sections);
        List<Action> axnSet = getActions(sections);
        Precondition goalCon = getGoal(sections);
        
        /* get the abstract state using landmarks */
        AbstractState abstractInit = concreteInit.getAbstraction(landmarks);
        
        return new Domain(abstractInit, axnSet, goalCon, landmarks);
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
    
    
    private static List<Inequality> getInequalities(String section, Boolean useForLandmarks){
        /*section is a comma separated list of variable/constant inequalities*/
        String assignmentTuple[], variable;
        Integer value;
        List<Inequality> inequalities = new ArrayList<Inequality>();
        
        for (String assignment:section.split(", *")){
            inequalities.add(new Inequality(assignment));
        }
        
        /* Identify landmarks from inequalities*/
        if (useForLandmarks){
            setLandmarks(inequalities);
        }
        return inequalities;
    }
    
    
    private static ConcreteState getConcreteInitState(Map<String, String> sections){
        String initSection = sections.get("Init");
        Map<String, Integer> stateEssentials = new HashMap<String, Integer>();
        Map<String, Boolean> stateEssentialsBool = new HashMap<String, Boolean>();
        
        List<Inequality> inequalities = getInequalities(initSection, false);
        
        for (Inequality ineq:inequalities){
            if (stateEssentials.containsKey(ineq.getVar()) || stateEssentialsBool.containsKey(ineq.getVar())){
                System.out.format("Error: concrete state specification reassigns value to variable. <%s>\n", ineq.getString());
                System.exit(-1);
            }
            if (ineq.isBoolean()){
               stateEssentialsBool.put(ineq.getVar(), ineq.getBoolVal()); 
            }
            else if (ineq.isAssignment()){
               stateEssentials.put(ineq.getVar(),ineq.getConstant());
            }
            else{
                System.out.format("Error: concrete state specified using unknown format %s\n", ineq.getString());
                System.exit(-1);
            }        
        }
        
        return new ConcreteState(stateEssentials, stateEssentialsBool);
    }  
    
    
    private static Precondition getPreconditions(String preconStr){
        return new Precondition(getInequalities(preconStr, true));
    }
    
    
    private static Map<String, Set<String>> getEffects(String effectStr){
        Map<String, Set<String>> effects = new HashMap<String, Set<String>>();
        
        effects.put("VI", new HashSet<String>());
        effects.put("VD", new HashSet<String>());
        effects.put("VT", new HashSet<String>()); //boolean vars set to T
        effects.put("VF", new HashSet<String>()); //boolean vars set to F
  
        for (String effect:effectStr.split(", *")){
            if (effect.contains("++")){
                effects.get("VI").add(effect.replace("++", "").trim());
            }
            else if (effect.contains("--")){
                effects.get("VD").add(effect.replace("--", "").trim());
            }
            else if (effect.contains("!")){
                effects.get("VF").add(effect.replace("!", "").trim());
            }
            else {
                effects.get("VT").add(effect.trim());
            }
        }        
        return effects;
        
    }
    
    
    private static List<Action> getActions(Map<String, String> sections){
        String preconAxn[], effectStr, axnSection = sections.get("Actions");
        String nameAxn[], axnName="";
        Precondition precon;
        List<Action> actionSet = new ArrayList<Action>();
        Map<String, Set<String>> effects;
        int i = 0;
        for (String axnStr:axnSection.split("\n")){
            if (axnStr.isEmpty()){
                continue;
            }
            nameAxn = axnStr.split("]");
            if (nameAxn.length == 2){
                axnName = nameAxn[0].replace("[", "");
                axnStr = nameAxn[1];
                System.out.format("Got action name %s ", axnName);
            }
            preconAxn = axnStr.split(":");
            /* If the precon or effects refer to a landmarked variable, this action
             * needs to be compiled.
             */               
            precon = getPreconditions(preconAxn[0]);
            effects = getEffects(preconAxn[1]);
            if (axnName.equals("")){
                axnName = "A" + Integer.toString(i);
            }
            Action a = new Action(effects.get("VI"), effects.get("VD"), effects.get("VF"), effects.get("VT"), 
                                    precon, axnName);
            actionSet.add(a);
            i+=1;
        }
        return actionSet;
    }

    
    private static Precondition getGoal(Map<String, String> sections){
        return getPreconditions(sections.get("Goal"));
    }
    
    
    private static void setLandmarks(List<Inequality> inequalities){  
        Integer value;
        
        for (Inequality ineq:inequalities){
            value = ineq.getConstant();
            if (ineq.isAssignment()){
                
            }
            if (ineq.getComparator().equals(">")){
                value = ineq.getConstant()+1;
            }
            landmarks.addToBunch(ineq.getVar(), value);
        }
        
        for (String variable:landmarks.getVars()){
            landmarks.addToBunch(variable, 0);
        }
    }
    
}
    

