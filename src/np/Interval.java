/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package np;

/**
 *
 * @author Sid
 */
public class Interval extends Value {
    /* 
     * Intervals will be closed below and open above.   
     */
    
    private Integer lowerBound = 0;
    private Integer upperBound = 0; //-1  indicates infinity
    
    Interval(int lb, int ub){
        lowerBound = lb;
        upperBound = ub;
    }
    
    public Interval getInterval(){
        return new Interval(lowerBound,upperBound);
    }
            
    public int getLB(){
        return lowerBound;
    }
    
    
    public int getUB(){
        return upperBound;
    }
    
    public Interval getNeighboringInterval(LandmarkBunch lbunch, int delta, String var){
        int newLB = lbunch.getNeighboringLandmark(var, lowerBound, delta);
        int newUB = lbunch.getNeighboringLandmark(var, upperBound, delta);
        
        if ((newLB == lowerBound) || (newUB == upperBound)){
            return this;
        }

        return new Interval(newLB, newUB);
    }
   
    
    @Override
    public String toString(){
        return "["+ lowerBound.toString() +  ", " + upperBound.toString() + ")";
    }
    
    
    public String toIneqString(String var){
        String ubString = upperBound.toString();
        if (ubString.equals("-1")){
            ubString = "INF";
        }
        return lowerBound.toString() + " <= " + var + " < " +  ubString;
    }
    
    public Boolean greaterOrEqualTo(Interval v){
        boolean lowerBoundOK = (lowerBound <= v.getLB());
        boolean upperBoundOK;
        
        int vUB = v.getUB();
        
        if (upperBound==-1){
            upperBoundOK = true;
        }
        else {
            if (vUB==-1){
                upperBoundOK = false;
            }
            else{
                upperBoundOK = (upperBound >= vUB);
            }
        }
       
       /* if ((lowerBound <= v.getLB()) && ( (upperBound >= v.getUB())||(upperBound ==-1))){*/
        if (lowerBoundOK && upperBoundOK){
            return true;
        }
        return false;
    }
    
    public Boolean equals(Interval i2){
        return ((this.getLB() == i2.getLB()) && (this.getUB() == i2.getUB()));
    }
    
}
