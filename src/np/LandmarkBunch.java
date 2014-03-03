/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package np;

import java.util.*;

/**
 *
 * @author Sid
 */
public class LandmarkBunch {
    private Map<String, List<Integer>> bunch = new HashMap<String, List<Integer>>();
   
    
    public Set<String> getVars(){
        return bunch.keySet();
    }
    
    
    public void addToBunch(String variable, Integer value){
        if (!bunch.containsKey(variable)){
            bunch.put(variable, new ArrayList<Integer>());
        }
        if (!bunch.get(variable).contains(value)){  
            bunch.get(variable).add(value);     
            /* ***TBD*** Use incremental O(lg(n)) sorted collection instead */
            Collections.sort(bunch.get(variable));      
        }

    }
    
    
    public String toString(){
        String outStr = bunch.toString();
        
        return outStr;
    }
    
    
    public List<Integer> getLandmarksFor(String var){
        return bunch.get(var);        
    }
    
    
    public Integer getNeighboringLandmark(String var, int lmark, int delta){
        if (delta==-1){
            return getPreviousLandmark(var, lmark);
        }
        else {
            return getNextLandmark(var, lmark);
        }
    }
    
    public Integer getPreviousLandmark(String var, Integer lmark){
        if (lmark == 0) {
            return 0;
        }
        
        int index;
        
        if (lmark == -1) {
            index = bunch.get(var).size()-1;
            return bunch.get(var).get(index);
        }
        
        index = bunch.get(var).indexOf(lmark);
        if (index == 0) {
            return lmark;   
        }
        else{
            return bunch.get(var).get(index-1);
        }   
    }
    
    
    public Integer getNextLandmark(String var, Integer lmark){
        if (lmark == -1){
            return lmark;
        }
        
        int index = bunch.get(var).indexOf(lmark);
        if (index == bunch.get(var).size()-1) {
            return -1;   
        }
        else{
            return bunch.get(var).get(index+1);
        }   
    }
    
    
    public List<Assignment> getAssignments(){
        List<Assignment> asmtList = new ArrayList<Assignment>();
        
        for (String variable:bunch.keySet()){
            for (Integer value:bunch.get(variable)){
                asmtList.add(new Assignment(variable, value));
            }
        }
        
        return asmtList;
    }
    
    
    public int getNextLandmarkForValue(String var, int value){
        int ub = -1;
        for (int i=0;i<bunch.get(var).size();i++){
            if (value<bunch.get(var).get(i)){
                ub = bunch.get(var).get(i);
                break;
            }
        }
        return ub;
    }
    
    
    public int getPreviousLandmarkForValue(String var, int value){
        int lb = 0;
        
        for (int i=bunch.get(var).size()-1; i>=0; i--){
            if (value>=bunch.get(var).get(i)){
                lb = bunch.get(var).get(i);
                break;
            }
        }
        
        return lb;      
    }
    
    
    public Interval getEnclosingInterval(String var, int value){        
        
        int ub = getNextLandmarkForValue(var, value);
        int lb = getPreviousLandmarkForValue(var, value);
        
        return new Interval(lb, ub);
    }
    
}
