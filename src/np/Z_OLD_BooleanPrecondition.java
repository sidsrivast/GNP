/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package np;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class for representing conditions on variables. Used as preconditions and goal.
 * 
 * posPrecons -- set of variables that must be positive
 * negPrecons --  .............................negative
 * zeroPrecons -- .............................zero
 * 
 * @author siddharth
 */
public class BooleanPrecondition {
    private Set<String> posPrecons;
    private Set<String> negPrecons;
    private Set<String> zeroPrecons;
    private Map<String, Integer> rawMap;
    
    
    BooleanPrecondition(){
        posPrecons = new HashSet<String>();
        negPrecons = new HashSet<String>();
        zeroPrecons = new HashSet<String>();
        rawMap = new HashMap<String, Integer>(); //only defined if Preconditions set usign setPrecon
    }
    
    
    BooleanPrecondition(Set<String> pos, Set<String> neg, Set<String> zero){
        posPrecons = new HashSet<String>(pos);
        negPrecons = new HashSet<String>(neg);
        zeroPrecons = new HashSet<String>(zero);
        rawMap = new HashMap<String, Integer>(); //only defined if Preconditions set usign setPrecon        
    }

    /*
     * Initialize from a map from vars to integers. See setPrecon.
     */
    BooleanPrecondition(Map<String, Integer> preconMap){
        this();
        for (String variable:preconMap.keySet()){
            setPrecon(variable, preconMap.get(variable));
        }
    }
    
    
    public Integer getRawValue(String var){
        if (this.rawMap.keySet().contains(var)){
            return rawMap.get(var);}
        else {
            return null;}
    }
    
    
    public void addFrom(BooleanPrecondition precon2){
        posPrecons.addAll(precon2.posPrecons);
        negPrecons.addAll(precon2.negPrecons);
        zeroPrecons.addAll(precon2.zeroPrecons);
        if (precon2.rawMap.size()>0){
            for (String var:precon2.rawMap.keySet()){
                rawMap.put(var, precon2.getRawValue(var));
            }
        }
    }


    public final void setPrecon(String variable, Integer value){
        rawMap.put(variable, value);
        if (value>0){
            posPrecons.add(variable);
        }
        else if (value == 0){
            zeroPrecons.add(variable);
        }
        else {
            negPrecons.add(variable);
        }
    }
    
    
    
    
    @Override
    public String toString(){
        return toString("raw", true);
    }
    
    
    public String toPDDLString(){
        return toString("PDDL", true);
    }
    
    
    public String toPDDLGoalString(){
        return toString("PDDL", false);
    }
    
    
    /*
     * Return a readable string form of precons.
     */
    public String toString(String mode, boolean qmark){
        String s = new String();
        String obj;
        
        if (qmark){
            obj = "?o";
        }
        else{
            obj = "o1";
        }
        
        if (mode.equals("raw")){
            return rawMap.toString();
        }
        
        for (String var:posPrecons){
            if ("PDDL".equals(mode)){
                s += "(> (" + var + " " + obj + ") 0) ";
            }
            else{
                s=s.concat(var+">0; ");
            }
        }
        
        for (String var:negPrecons){
            if ("PDDL".equals(mode)){
                s += "(< (" + var + " "+ obj +") 0) ";
            }
            else{
                s=s.concat(var+"<0; ");
            }
        }
        
        for (String var:zeroPrecons){
            if ("PDDL".equals(mode)){
                s += "(= (" + var + " "+ obj +") 0) ";
            }
            else{
                s=s.concat(var+"=0; ");           
            }
        }
        
        if ("PDDL".equals(mode)){
            return "(and " + s + ")" ;
        }

        return s;
    }

    
    /*
     * Test if the given state's vars satisfy the precons
     */
    public boolean satisfied(AbstractState s){
        for (String var:posPrecons){
            if (!(s.isPos(var))){
                return false;
            }
        }
        
        for (String var:negPrecons){
            if (!(s.isNeg(var))){
                return false;
            }
        }

        for (String var:zeroPrecons){
            if (!(s.isZero(var))){
                return false;
            }
        }
        return true;
    }
}
