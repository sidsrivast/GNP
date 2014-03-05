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
    Action(Set<String> VI, Set<String> VD, Precondition precon, String axnName ){
        varsDecremented = new HashSet<String>(VD);
        varsIncremented = new HashSet<String>(VI);
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
            LinkedList<HashMap<String, Interval>> stateQ){
        Set<AbstractState> absStateSet = new HashSet<AbstractState>();
        
        AbstractState as;
        Map<String, Interval> abstractStateEssentials;
        
        ListIterator lit = stateQ.listIterator();
        while (lit.hasNext()){
            abstractStateEssentials = (Map<String, Interval>) lit.next();
            as = new AbstractState(abstractStateEssentials);
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
            Set<Interval>> possibleValues){

        LinkedList<HashMap<String, Interval>> stateQ = 
                new LinkedList<HashMap<String, Interval>>();

        stateQ.addLast(new HashMap<String, Interval>());
        
        int dimension = possibleValues.size();
        String currentVariable;
        
        HashMap<String, Interval> partialState, partialStateX;

        
        while (true){
            partialState = stateQ.removeFirst();

            if (partialState.size() == dimension){
                stateQ.add(partialState);
                return makeAbsStateSet(stateQ);
            }
            
            currentVariable = getUnassignedVariable(partialState.keySet(), 
                    possibleValues.keySet());
            
            for (Interval  intvl: possibleValues.get(currentVariable)){
                partialStateX = new HashMap<String, Interval>(partialState);
                partialStateX.put(currentVariable, intvl);
                stateQ.addLast(partialStateX);
            }
        }
    }
    

    /*
     * Get a mapping from each variable to the set of possible values it can 
     * have after an application of this action.
     */
    private HashMap<String, Set<Interval>> getPossibleValues(AbstractState s, LandmarkBunch lbunch) {
        Iterator<String> x;
        HashMap<String, Set<Interval>> possibleValues = 
                new HashMap<String, Set<Interval>>();
        String v;
        HashSet<String> allVars = new HashSet<String>();
        
        x = varsDecremented.iterator();
        
        while (x.hasNext()){
            v = x.next();
            //possibleValues.put(v, s.getInterval(v).decrement(lbunch.getLandmarksFor(v)));
            possibleValues.put(v, new HashSet<Interval>());
            possibleValues.get(v).add(s.getInterval(v));
            possibleValues.get(v).add(s.getInterval(v).getNeighboringInterval(lbunch, -1, v));
        }
        
        x = varsIncremented.iterator();
        while (x.hasNext()){
            v = x.next();
            possibleValues.put(v, new HashSet<Interval>());
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
                possibleValues.put(v, new HashSet<Interval>());
                possibleValues.get(v).add(s.getInterval(v));
            }
        }

        return possibleValues;
    }
    
}
