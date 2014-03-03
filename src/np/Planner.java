/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package np;
import java.io.*;
import java.util.*;

/**
 * Basic methods to invoke a planner using a system call. 
 * @author Sid
 */
public class Planner{
    private String plannerPath;
    private String plannerArgs;
    private String outFile = "/tmp/out.txt";
    
    Planner(String path, String args){
        plannerPath = path;
        plannerArgs = args;
    }
    
    
    /*
     * Invoke the planner. Returns a map consisting of a return value and
     * the planner's text output.
     */
    public Map invoke(long timeAllowed)
            throws InterruptedException
    {
        String outputLine;
        String result="";
        int retVal=-1;
        Map resultMap = new HashMap();
        
        try {
            Process child = Runtime.getRuntime().exec(plannerPath + " "+ plannerArgs);
            InputStream output = child.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(output));
        
            long start = System.currentTimeMillis()/1000; 
            long timeElapsed = 0;
            
            while (((outputLine=in.readLine())!=null) && (timeElapsed<timeAllowed)){
                // in.readline blocks while it waits for the next line
                // of the end of stream.
                // thus the condition above will be satisfied after reading the 
                // the first line after timeAllowed is over.
                
                // It will not be evaluated if timeAllowed is over but
                // in.readline is blocking.
                
                result = result.concat(outputLine).concat("\n");
                timeElapsed = System.currentTimeMillis()/1000 - start;
            }
            
            try{
                retVal = child.exitValue();
            }        
            catch(IllegalThreadStateException e){
                child.destroy();
                System.out.println("Allocated time over. Time elapsed = "+timeElapsed+"s\n");
                retVal = -1;
            }
            
            resultMap.put(new Integer(1), retVal);
            resultMap.put(new Integer(2), result);     
        }
        catch (IOException e){
            System.err.println(e.getMessage());
        }
        
    Utils.writeToFile(result, outFile);        
    return resultMap;
    }
    
}
