/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package np;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Compiles actions with landmarks into 0/1 comparison actions.
 * Instantiate with an action with landmarks. Create a bunch of compiled actions
 * corresponding to it.
 * 
 * @author Sid
 */
public class AxnCompiler {
    private Action originalAction;
    
    private static List compiledActions = new ArrayList<Action>();
    
    public AxnCompiler(Action inputAxn){
        originalAction = inputAxn;
        
        
    }
    
}
