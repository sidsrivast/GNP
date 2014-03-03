/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package np;

import java.util.List;

/**
 *
 * An abstract class denoting what every planner output must have: 
 * the output string. Different planner-specific refinements set up the begin 
 * and end markers as well as methods to get the plan actions.
 *
 * 
 * 
 *  * Never use directly. Use a class like FFOutput instead.
 * 
 * @author Sid
 */
abstract class PlannerOutput {
    private String outputString;
    
    PlannerOutput(String text){
        outputString = text;
    }
    
    public String getBeginMarker(){
        return "";
    }
    
    public String getEndMarker(){
        return "";
    }
    
    
    /*
     * Return the substring between begin and end markers. To be used
     * in all planner-specific classes.
     * 
     */
    public String getPlanText(String begin, String end){
        String plan;
        String planBeginMarker = begin;
        String planEndMarker = end;
        
        int planBeginOffset = outputString.indexOf(getBeginMarker())+getBeginMarker().length()-1;
        int planEndOffset = outputString.indexOf(getEndMarker());
        
        plan = outputString.substring(planBeginOffset, planEndOffset);
        
        return plan;
    }
    
    public abstract List<String> getPlanActions();

    /**
     * @return the outputString
     */
    public String getOutputString() {
        return outputString;
    }

    /**
     * @param outputString the outputString to set
     */
    public void setOutputString(String outputString) {
        this.outputString = outputString;
    }
}
