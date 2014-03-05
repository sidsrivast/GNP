/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package np;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 *Extends the PlannerOutput class with specific methods for extracting
 * plan actions, and begin/end markers.
 * 
 * @author Sid
 */
public class FFOutput extends PlannerOutput {
    FFOutput(String output){
        super(output);
    }
    

    @Override
    public String getBeginMarker(){
        return "follows\n\nstep ";
    }
    
    @Override
    public String getEndMarker(){
        return "time spent";
    }
    
    
    @Override
    public List<String> getPlanActions(){
        List<String> planActions = new ArrayList<String>();
        
        ArrayList<String> lines = new ArrayList<String>(Arrays.asList(
                this.getPlanText(getBeginMarker(), getEndMarker()).split("\n")));
        
        for (String line: lines){
            System.out.println("Line >>"+line+"<<");
            String[] indexAxn = line.split(":");
            if (indexAxn.length == 1) {
                System.out.println("Found no `:'. Stopping search for actions.");
                break;
            }
            planActions.add(indexAxn[1].trim().replaceFirst(" ", "; ").toLowerCase());
        }
        
        System.out.println("Extracted plan with " + 
                Integer.toString(planActions.size()) + " actions.\n");
        return planActions;
    }
}
