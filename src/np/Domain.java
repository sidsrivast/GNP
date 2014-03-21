/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package np;

import java.io.*;
import java.util.*;

/**
 * A domain consists of a set of actions, an initial abstract state, and a goal
 * condition. Contains methods for conversion to PDDL format.
 * 
 * actionMap -- map from action names to action objects
 * initialState -- initial abstract state
 * goal condition -- condition expressed in the same format as a precondition
 * 
 * @author siddharth
 */
public class Domain {
    public Map<String, Action> actionMap = new HashMap<String, Action>();
    public AbstractState initialState;
    public Precondition goalCondition;
    public LandmarkBunch landmarks;
    
    Domain(AbstractState init, List<Action> axnSet, Precondition goal, 
            LandmarkBunch lmarks){
        for (Iterator<Action> it = axnSet.iterator(); it.hasNext();) {
            Action axn = it.next();
            String text = axn.getName();
            actionMap.put(axn.getName(), axn);
        }
        
        landmarks = lmarks;
        initialState = init;
        goalCondition = goal;
    }
    
    @Override
    /*
     * A readable, text version.
     */
    public String toString(){
        String s;
        
        s  = "Initial  state: "+ initialState.toString() + "\n";
        s += "Goal condition: "+ goalCondition.toString() + "\n";
        s += "Actions:"+"\n";
        
        for (Action axn: this.getActions()){
            s += " "+axn.toString()+"\n";
        }
        
        s += landmarks.toString();

        return s;
    }
    
    
    public Action getAction(String name){
        return actionMap.get(name);
    }
    
    
    public Set<Action> getActions(){
        Set<Action> axnSet = new HashSet<Action>();
        for (Iterator<String> it = actionMap.keySet().iterator(); it.hasNext();) {
            String axnName = it.next();
            axnSet.add(actionMap.get(axnName));
        }
        return axnSet;
    }
    
    
    /*
     * Returns a string containing the pddl domain file description.
     */
    public String toDomainPDDLString(){
        String pddlString;
        
        String fixedDec = "(define (domain numericInstance)", functionDec, axnDec;
        
        fixedDec += "\n\t(:requirements :typing :fluents)";
        fixedDec += "\n\t(:types dtype)";

        functionDec = "\n\t(:functions";        
        for (String var:initialState.getVars()){
            functionDec += "\n\t\t("+ var+" ?o - dtype)";
        }
        functionDec += ")\n";
        
        String predicateDec = "(:predicates";
        for (String var:initialState.getBoolValues().keySet()){
            predicateDec += "\n\t\t(" + var + " ?o - dtype)";
        }
        predicateDec += ")\n";
        
        axnDec = "";
        Integer i=0;
        
        for (Action axn: this.getActions()){
            axnDec += axn.toPDDLString();
            i++;
        }
        return fixedDec + functionDec + predicateDec + axnDec +")\n";
    }
    
    
    public String toProblemPDDLString(ConcreteState s){
        String fixedInit = "";
        
        fixedInit =  "(define (problem problem-instance)\n";
        fixedInit += " (:domain numericInstance)\n";
        fixedInit += " (:objects\n";
        fixedInit += "     o1 - dtype)\n";
        fixedInit += " (:init \n";
        
        String valuesPDDL = s.toPDDLString() + " )\n";
        
        String goalStr = " (:goal " + goalCondition.toPDDLGoalString() + ")\n";
        
        return fixedInit + valuesPDDL + goalStr + "\n)";
    }
    
    
    public void writeToFile(String text, String path)
    {
        BufferedWriter myWriter = null;
        try{
            myWriter = new BufferedWriter(new FileWriter(path));
            myWriter.write(text);
        }
        catch(IOException e){
            System.out.println("Error writing file " + path);
            System.out.println(e.getMessage());
            
        }
        finally {            
            if (myWriter!=null){
                try{
                    myWriter.close();
                }
                catch(IOException e){
                    System.out.println(e.getMessage());
                }
            }
            
        }
     
    }
    
    
    public void toDomainPDDLFile(String path){
        Utils.writeToFile(this.toDomainPDDLString(), path);
    }
    
    
    public void toProblemPDDLFile(String path, ConcreteState s){
        Utils.writeToFile(this.toProblemPDDLString(s), path);
    }
    
    
    public LandmarkBunch getLandmarks(){
        return this.landmarks;
    }
    
}
