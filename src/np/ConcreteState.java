/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package np;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A mapping from variable names to integer values.
 * @author siddharth
 */
public class ConcreteState extends AbstractState {
    //private Map<String, Integer> varValues = new HashMap<String, Integer>();
            
    ConcreteState(){
        //this(10);
    }
    
    ConcreteState(int n){
        for (int i=1;i<=n;i++){
        this.setValue("x"+i, 0);
        }
    }
    
    
     ConcreteState(Map<String, Integer> stateEssentials){
        for (String x: stateEssentials.keySet()){
            setValue(x, stateEssentials.get(x));
        }
    }
  

     public void setValue(String x, int n){
        getRawValues().put(x, n);
        this.setInterval(x, new Interval(n, n+1));
     }

    
    public String toPDDLString(){
        
        String PDDLStr;
        
        PDDLStr = "";
        
        for (String var:getRawValues().keySet()){
            PDDLStr += "\t(=" + " (" + var + " o1) " + getRawValues().get(var).toString()
                    +  ")\n";
        }
        return PDDLStr;
    }
    
    
    public int getValue(String var){
        return getRawValues().get(var);
    }
    
    
    public AbstractState getAbstraction(LandmarkBunch lbunch){
        return new AbstractState(getRawValues(), lbunch);
    }
}
