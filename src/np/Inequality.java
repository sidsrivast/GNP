/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package np;

/**
 *
 * @author Sid
 */
public class Inequality {
    private String rawString;
    private String var;
    private String comparator="";
    private int constant;
    
    Inequality(String str){
        rawString = str;
        
        if (str.indexOf("=")>-1){
            comparator = "=";
        }
        if (str.indexOf(">")>-1){
            comparator = ">";
        }
        if (str.indexOf("<")>-1){
            comparator = "<";
        }
        
        String[] assignmentTuple = str.split(comparator);
        
        if (assignmentTuple.length<2) {
            System.out.format("Error: ill-formed inequality: %s\n", str);
            System.exit(-1);
        }
        var = assignmentTuple[0].trim();
        constant = Integer.parseInt(assignmentTuple[1].trim());
        
    }
    
    public String getVar(){ return var;}
    
    public String getComparator(){return comparator;}
    
    public int getConstant(){return constant;}
    
    public String getString(){return rawString;}
    
    public Boolean isAssignment(){
        if (comparator=="="){return true;}
        return false;
    }
    
    
    
}
