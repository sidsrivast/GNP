
package np;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * Includes methods for setting and printing abstract states. Values are Integer,
 * but limited to 0,1 since this is an abstract state.
 * 
 * Extension for multiple landmarks: abstractValues
 * 
 * @author siddharth
 */
public class AbstractState {
    private Map<String, Integer> rawValues = new HashMap<String, Integer>();
    private Map<String, Interval> varIntervals = new HashMap<String, Interval>();        
            
    
    AbstractState(int n, LandmarkBunch lbunch){
        int i;
        
        for (i=1;i<=n;i++){
           this.setValue("x"+i, 0, lbunch);
        }
    }
    
    
    AbstractState(Map<String, Integer> origRaw, Map<String, Interval> origIntervals){
        rawValues.putAll(origRaw);
        varIntervals.putAll(origIntervals);
    }
    
    AbstractState(){
        //this(10);
    }
    
    
    AbstractState(Map<String, Interval> abstractEssentials){
        for (String var: abstractEssentials.keySet()){
            setInterval(var, abstractEssentials.get(var));
        }
    }
    
    AbstractState(Map<String, Integer> stateEssentials, LandmarkBunch lbunch){
        for (String x: stateEssentials.keySet()){
            setValue(x, stateEssentials.get(x), lbunch);
        }
    }
    
    
    protected void setInterval(String var, Interval ivl){
        varIntervals.put(var, ivl);
    }
    
    
    public AbstractState getCopy(){        
        return  new AbstractState(getRawValues(), varIntervals);
    }
    
    
    public int getDimension(){
        return varIntervals.size();
    }

    
    public void setValue(String x, int n, LandmarkBunch lbunch){
        /*
         * Sets abstract value corresponding to any assignment n to variable x.
         * Uses set of landmarks to determine the class for each variable.
         */
        getRawValues().put(x, n);     
        varIntervals.put(x, lbunch.getEnclosingInterval(x, n));   
    }

    
    public Interval getInterval(String x){
        return varIntervals.get(x);
    }
    

    public Set<String> getVars(){
        return varIntervals.keySet();
    }
    
    
    public boolean equivalent(AbstractState s){
        Interval thisInterval, sInterval;
        
        for (String var:this.varIntervals.keySet()){
            thisInterval = getInterval(var);
            sInterval = s.getInterval(var);
            if (!thisInterval.greaterOrEqualTo(sInterval) || !sInterval.greaterOrEqualTo(thisInterval)){
                return false;
            }
        }

        return true;
    }
    
    /*Returns text description of state in long form.
     * 
     */
    @Override
    public String toString(){

        SortedSet<String> variables = new TreeSet<String>(varIntervals.keySet());
        String s = new String();
        
        for (String var: variables){
            s+=var+"="+varIntervals.get(var).toString()+"; ";
        }
        
        if (!rawValues.isEmpty()){
            s+=" RAW: ";
            for (String var:variables){
                s+=var+"="+getRawValues().get(var).toString()+"; ";
            }
        }
        return s;
    }
    
    
    public String toStringShort(){
        /*Returns a shorter description, appropriate for labeling output
         * graphs.
         * 
         */
        SortedSet<String> variables = new TreeSet<String>(varIntervals.keySet());
        String s = new String();
        
        for (String var: variables){
            s+= var+"="+ varIntervals.get(var).toString()+" ";
        }
        return s;
    }
    
    
    /*
     * Uses a random number generator to create a concrete state
     * subsumed by this abstract state. Each non-zero variable gets a 
     * value between LB and UB.
     * 
     * Use of LB depricated.
     */  
    public ConcreteState getInstance(int LB, int UB){
 
        //LB not used
        
        ConcreteState CS = new ConcreteState();
        Random rvGenerator = new Random();
        int varLB, varUB;    
        
        
        for (String var:varIntervals.keySet()){
            System.out.println("variable: "+var);
            varLB = this.varIntervals.get(var).getLB();
            
            if (this.varIntervals.get(var).getUB() == -1) {
                varUB = UB;
            }
            else{
                varUB = Math.min(this.varIntervals.get(var).getUB(), UB);
            }
           
            if (varUB-varLB >0){
                CS.setValue(var, varLB + rvGenerator.nextInt(varUB-varLB));
            } 
            else{
            CS.setValue(var, varLB);
            }
        }
        return CS;
    }

    /**
     * @return the rawValues
     */
    public Map<String, Integer> getRawValues() {
        return rawValues;
    }

    /**
     * @param rawValues the rawValues to set
     */
    public void setRawValues(Map<String, Integer> rawValues) {
        this.rawValues = rawValues;
    }
}
