/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package np;

/**
 *
 * @author Sid
 */
public class Assignment {
    private String var;
    private String op;
    private Integer value;
    private Integer landmark = null;
    
    Assignment(String s){
        /*
         * 
         * Handles xi=ci or xi>0 
         */
        
        String tuple[];
        
        tuple = s.split("=");
        if (tuple.length <2){
            tuple = s.split(">");
        }
        
        var = tuple[0];
        value = Integer.parseInt(tuple[1].trim());
        if (value != 0){
            landmark=value;
        }
    }
    
    Assignment(String s, Integer v){
        var =s;
        value = v;
    }
    
    public String getVar(){ return var;}
    
    public Integer getVal(){ return value;}
    
    public String getOp(){ return op;}
    
    public Integer getLandmark(){ return landmark;}
    
}
