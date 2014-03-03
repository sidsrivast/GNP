/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package np;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Sid
 */
public class LandmarkCompiler {
    private String inputFName = "";
    Domain originalD;
    
    LandmarkCompiler(String inputFName){
        this.inputFName = inputFName;
        originalD = NPinput.getDomainFromFile(inputFName); 
    }
    
    
    
    Domain getCompiledDomain()
            throws java.io.IOException 
    {
        List<Action> compiledActions = getCompiledActions();
        AbstractState compiledInit = getCompiledInit();
        BooleanPrecondition compiledGoal = getCompiledGoal();
       
        Domain compileD = Domain(compiledInit, compiledActions, compiledGoal);                
        return compileD;
    }
    
    
    List<Action> getCompiledActions(){
        List<Action> compAxns = new ArrayList<Action>)();
        
        for (Action axn:originalD.getActions()){
            compAxns.addAll(axn.compile());
        }
        
        return compAxns;
    }
}
