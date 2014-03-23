/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package np;

/**
 *
 * @author Sid
 */
public class Value {
    
    

 public Boolean getValue(){
        System.out.println("Must be overriden");
        return null;
    }

 
 public Interval getInterval(){
        System.out.println("Must be overriden");
        return null;
    }
 
 public String toIneqString(String var){
     return "ineq form not implemented";
 }

}
