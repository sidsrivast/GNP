/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package np;

import java.util.*;

/**
 * Class for representing conditions on variables. Used as preconditions and goal.
 * 
 * @author siddharth
 */
public class Precondition {
    private Map<String, Interval> preconMap = new HashMap<String, Interval>(); 
    private Map<String, Boolean> boolMap = new HashMap<String, Boolean>();
    
    Precondition(){}
    
    Precondition(Map<String, Boolean> booleans){
        boolMap.putAll(booleans);
    }
    
    Precondition(List<Inequality> inequalities){
        Map<String, Integer> ubMap = new HashMap<String, Integer>();
        Map<String, Integer> lbMap = new HashMap<String, Integer>();
        Map<String, Integer> appropriateMap= null;
        Set<String> varSet = new HashSet<String>();
        int bound = 0;
        Boolean tval = null;
        
        
        for (Inequality ineq: inequalities){
            if (ineq.getComparator().equals(">")) {
                appropriateMap = lbMap;
                bound = ineq.getConstant()+1;
                varSet.add(ineq.getVar());
            }
            
            else if (ineq.getComparator().equals("<")){
                appropriateMap = ubMap;
                bound = ineq.getConstant();
                varSet.add(ineq.getVar());
            }
                              
            if (appropriateMap != null && (!appropriateMap.containsKey(ineq.getVar()))){
                    appropriateMap.put(ineq.getVar(), bound);
            } 
            else if (appropriateMap != null){
                    System.out.format("Error: two lower bounds for %s in "
                            + "precondition: %s\n", ineq.getVar(), ineq.getString());
                    System.exit(-1);
            }
            
            if (ineq.getComparator().equals("=")){
                if (!ubMap.containsKey(ineq.getVar()) && !lbMap.containsKey(ineq.getVar())){
                    ubMap.put(ineq.getVar(), ineq.getConstant()+1);
                    lbMap.put(ineq.getVar(), ineq.getConstant());
                    varSet.add(ineq.getVar());
                }
                else{
                    System.out.format("Error: two lower bounds for %s in "
                            + "precondition: %s\n", ineq.getVar(), ineq.getString());
                    System.exit(-1);
                }
            }
            
            if (ineq.isBoolean()){
                boolMap.put(ineq.getVar(),ineq.getBoolVal());
            }
            else{
                /* Convert ub, lb lists to intervals */
                int ub, lb;
                for (String var:varSet){
                    ub = -1;
                    lb = 0;
                    if (ubMap.containsKey(var)){ ub = ubMap.get(var);}
                    if (lbMap.containsKey(var)){ lb = lbMap.get(var);}
                    preconMap.put(var, new Interval(lb, ub));
                }
            }
        }
    }
       
    
    public Map<String, Interval> getPreconMap(){
        return this.preconMap;
    }
    
    public Map<String, Boolean> getBoolMap(){
        return this.boolMap;
    }
    
    public void addFrom(Precondition precon2){
        Map<String, Interval> srcMap = precon2.getPreconMap();
        for (String var:srcMap.keySet()){
            preconMap.put(var.toString(), new Interval(srcMap.get(var).getLB(),
                                                       srcMap.get(var).getUB()));
        }
        Map<String, Boolean> srcBoolMap = precon2.getBoolMap();
        for (String var:srcBoolMap.keySet()){
            boolMap.put(var.toString(), srcBoolMap.get(var));
        }    
    }
    
       
    
    @Override
    public String toString(){
        return toString("raw", true, false);
    }
        
    public String toPDDLString(){
        return toString("PDDL", true, false);
    }
      
    public String toPDDLGoalString(){
        return toString("PDDL", false, false);
    }
    
    public String toPDDLInitString(){
        return toString("PDDL", false, true);
    } 
    /*
     * Return a readable string form of precons.
     */
    public String toString(String mode, boolean qmark, boolean initMode){
        String s = new String();
        String obj;
        
        if (qmark){
            obj = "?o";}
        else{
            obj = "o1";}
        
        if (mode.equals("raw")){
            for (String var:preconMap.keySet()){
                s += var + " -> " + preconMap.get(var).toString() +"; ";
            }
            for (String var:boolMap.keySet()){
                s += var + " -> " + boolMap.get(var).toString() +"; ";
            }
            s += "\n";
        }
        
        Integer lb, ub;
        if (mode.equals("PDDL")){
            for (String var:preconMap.keySet()){
                lb = preconMap.get(var).getLB();
                ub = preconMap.get(var).getUB();
                
                if (lb == ub-1) {
                    s += "(= (" + var + " " + obj + ") "+ lb.toString() +") ";
                } 
                else{
                    s += "(>= (" + var + " " + obj + ") "+ lb.toString()+ ") ";
                    if (ub!=-1){
                        s += "(< (" + var + " " + obj + ") " + ub.toString() + ") ";
                    }
                }
            }
            String terminator, negatedVal;
            for (String var:this.boolMap.keySet()){
                terminator = "";
                negatedVal = "";
                if (!this.boolMap.get(var)){
                    if (!initMode){
                        negatedVal = "(not ";
                        terminator = ")";
                    }else{
                        continue;}
                }
                 s += negatedVal + "(" + var + " " + obj + ")" + terminator;                 
            }
            if (!initMode){
                s="(and " + s + ")";}
            

            
        }
        
        

        return s;
    }

    
    /*
     * Test if the given state's vars satisfy the precons
     */
    public boolean satisfied(AbstractState s){

        Boolean satisfied = true;
        
        for (String var:preconMap.keySet()){
            if (!preconMap.get(var).greaterOrEqualTo(s.getInterval(var))){
                satisfied = false;
                break;
            }
        }
        
        for (String var:this.boolMap.keySet()){
            satisfied = (this.boolMap.get(var) == s.getBoolValues().get(var));
            if (!satisfied) {
                break;
            }
        }
        
        return satisfied;
    }
}
