/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package np;

/**
 *
 * @author Sid
 */
public class Bool extends Value {
    public Boolean val = null;
   
    
    Bool(Boolean input){
        val = input;
    }
    
    public Boolean getValue(){
        return this.val;
    }
    
    public String toString(){
        return this.val.toString();
    }
}
