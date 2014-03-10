/*
 * Main implementation of actions: representation and application.
 */
package np;

import java.util.*;

/**
 *
 * @author siddharth
 */
public class Action {
    /*
     * name -- name of action
     * varsDecremented, varsIncremented -- sets of variable names being 
     *                                     decremented/incremented
     * precons -- action preconditions. See class Precondition.
     */
    private String name;
    private Set<String> varsDecremented;
    private Set<String> varsIncremented;
    private Set<String> boolVars;
    private Set<String> varsMadeT;
    private Set<String> varsMadeF;
    private Precondition precons = new Precondition();
    
    /*
     * Initializes an action using sets of variables that have to be 
     * positive, zero, or negative. 
     * 
     * Negative precons are not supported yet.
     */

    
    /*
     * Initializes using a pre-constructed Precondition object.
     */
    Action(Set<String> VI, Set<String> VD, Set<String> VF, Set<String> VT, Precondition precon, String axnName ){
        varsDecremented = new HashSet<String>(VD);
        varsIncremented = new HashSet<String>(VI);
        varsMadeT = new HashSet<String>(VT);
        varsMadeF = new HashSet<String>(VF);
        name = axnName;        
        
        precons.addFrom(precon);
    }
    
    
    public String getEffectString(){
        return varsDecremented.toString()+"--; "+varsIncremented.toString()+"++";
    }
    
    public String getName(){
        return this.name;
    }
    
    
    public Set<String> getDecVars(){
        return varsDecremented;
    }
    
    
    public Set<String> getIncVars(){
        return varsIncremented;
    }
   
    
    public Precondition getPrecons(){
        return this.precons;
    }
    
    
    public String toString(){
        String s;
        
        s = name + "\n      Preconditions: ";
        s += precons.toString();
        s += "\n      Effects: ";
        
        for (String var:varsDecremented){
            s+= var+"--; ";
        }
        
        for (String var:varsIncremented){
            s+= var+"++; ";
        }
        
        for (String var:this.varsMadeF){
            s+= var+"=F";
        }
        
        for (String var:this.varsMadeT){
            s+= var+"=T";
        }
        s+="\n";
        
        return s;
    }
    
    /*
     * Returns the action in PDDL form
     */
    public String toPDDLString(){
        String init, paramStr, preconStr, effectStr;
        
        init = " (:action" + " "+ name +"\n";
        paramStr = "\t:parameters (?o - dtype)\n";
        preconStr = "\t:precondition "+precons.toPDDLString()+"\n";
        
        effectStr = "\t:effect (and \n";
        for (String var:varsDecremented){
            effectStr += "\t\t(decrease ("+ var +" ?o)" + " 1)\n";
        }
        for (String var:varsIncremented){
            effectStr += "\t\t(increase ("+ var +" ?o)" + " 1)\n";
        }
        for (String var:this.varsMadeF){
            effectStr += "\t\t(not (" + var + " ?o))\n";
        }
        for (String var:this.varsMadeT){
            effectStr += "\t\t(" + var + " ?o)\n";
        }
        effectStr += "\t\t)";
        
        return init + paramStr + preconStr + effectStr + "\n )\n\n";
               
    }
    
    /*
     * Applies this action on a given concrete state. Straightforward 
     * increment/decrement ops on variables.
     */
    public ConcreteState applyAction(ConcreteState s){
        if (precons.satisfied(s)){
            for (String var:this.varsDecremented){
                s.setValue(var, s.getValue(var)-1);
            }
            for (String var:this.varsIncremented){
                s.setValue(var, s.getValue(var)+1);
            }
            for (String var:this.varsMadeF){
                s.setValue(var, false);
            }
            for (String var:this.varsMadeT){
                s.setValue(var, true);
            }
            return s;
        }
        else {
            return null;
        }
    }
    
    
    /*
     * Wrapper method for applying an action on an abstract state.
     * If precons are satisfied, calls getPossibleValue to get possible 
     * values for each variable and then a state generator which constructs
     * all possible combinations as result states.
     */
    public Set<AbstractState> applyAction(AbstractState s, LandmarkBunch lbunch){
        if (precons.satisfied(s)){
            return getStates(getPossibleValues(s, lbunch));
        }
        else
            return null;
    }       

    
    private String getUnassignedVariable(Set<String> assigned, Set<String> all){
        Set<String> diff = new HashSet<String>(all);
        all.removeAll(assigned);
        return (String) all.toArray()[0];
    }
        

    /*
     * Conversion from state-list to state set.
     */
    private Set<AbstractState> makeAbsStateSet(
            LinkedList<HashMap<String, Value>> stateQ){
        Set<AbstractState> absStateSet = new HashSet<AbstractState>();
        
        AbstractState as;
        Map<String, Value> abstractStateEssentialValues;
        
        ListIterator lit = stateQ.listIterator();
        while (lit.hasNext()){
            abstractStateEssentialValues = (Map<String, Value>) lit.next();
            as = new AbstractState(abstractStateEssentialValues);
            absStateSet.add(as);
        }
        
        return absStateSet;
    }
            
    /*
     * Returns set of abstract states corresponding to all possible combinations
     * of variable assignments given in the map possibleValues. This map is 
     * computed by getPossibleValues.
     */
    private Set<AbstractState> getStates(HashMap<String, 
            Set<Value>> possibleValues){

        LinkedList<HashMap<String, Value>> stateQ = 
                new LinkedList<HashMap<String, Value>>();
        int dimension = possibleValues.size();
        String currentVariable;
        HashMap<String, Value> partialState, partialStateX;
        stateQ.addLast(new HashMap<String, Value>());
              
        while (true){
            partialState = stateQ.removeFirst();
            if (partialState.size() == dimension){
                stateQ.add(partialState);
                return makeAbsStateSet(stateQ);
            }
            currentVariable = getUnassignedVariable(partialState.keySet(), possibleValues.keySet());
            
            for (Value  val: possibleValues.get(currentVariable)){
                partialStateX = new HashMap<String, Value>(partialState);
                partialStateX.put(currentVariable, val);
                stateQ.addLast(partialStateX);
            }
        }
    }
    

    /*
     * Get a mapping from each variable to the set of possible values it can 
     * have after an application of this action.
     */
    private HashMap<String, Set<Value>> getPossibleValues(AbstractState s, LandmarkBunch lbunch) {
        Iterator<String> x;
        HashMap<String, Set<Value>> possibleValues = new HashMap<String, Set<Value>>();
        String v;
        HashSet<String> allVars = new HashSet<String>();
        
        x = varsDecremented.iterator();
        
        while (x.hasNext()){
            v = x.next();
            //possibleValues.put(v, s.getInterval(v).decrement(lbunch.getLandmarksFor(v)));
            possibleValues.put(v, new HashSet<Value>());
            possibleValues.get(v).add(s.getInterval(v));
            possibleValues.get(v).add(s.getInterval(v).getNeighboringInterval(lbunch, -1, v));
        }
        
        x = varsIncremented.iterator();
        while (x.hasNext()){
            v = x.next();
            possibleValues.put(v, new HashSet<Value>());
            /* Test: [0,1) denotes the special case of [0,0]; increase guaranteed to move out of this
             interval */
            if (s.getInterval(v).getUB()!=1) {
                possibleValues.get(v).add(s.getInterval(v));
            }
            possibleValues.get(v).add(s.getInterval(v).getNeighboringInterval(lbunch, +1, v));
        }
        
        allVars.addAll(s.getVars());

        x = allVars.iterator();
        
        while (x.hasNext()){
            v = x.next();
            if (!(possibleValues.containsKey(v))) {
                possibleValues.put(v, new HashSet<Value>());
                possibleValues.get(v).add(s.getInterval(v));
            }
        }
        
        for (String var:s.getBoolValues().keySet()){
            possibleValues.put(var, new HashSet<Value>());

            if (this.varsMadeT.contains(var)){
                possibleValues.get(var).add(new Bool(true));
            }
            else if (this.varsMadeF.contains(var)){
                possibleValues.get(var).add(new Bool(false));
            }
            else {
                possibleValues.get(var).add(new Bool(s.getBoolValues().get(var)));
            }
        
        }

        return possibleValues;
    }
    
}
