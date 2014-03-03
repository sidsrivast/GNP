/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package np;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.Graphs;
/**
 * A set of miscellaneous i/o and graph methods.
 * 
 * @author Sid
 */
public class Utils {
    public static void writeToFile(String text, String path)
    {
        BufferedWriter myWriter = null;
        try{
            myWriter = new BufferedWriter(new FileWriter(path));
            myWriter.write(text);
        }
        catch(IOException e){
            System.out.println("Error writing file " + path);
            System.out.println(e.getMessage());
            
        }
        finally {            
            if (myWriter!=null){
                try{
                    myWriter.close();
                }
                catch(IOException e){
                    System.out.println(e.getMessage());
                }
            }
            
        }
    }
    
    
    public static String getAxnStrFromEdge(String e){
        return e.split("_")[0];
    }
   
    
    /*
     * Rtturn the graph (from package jgrapht) in dot format. Can be plotted 
     * using graphviz.
     * 
     */
    public static String graphToDotString(DirectedMultigraph<String, String> traceGraph, 
            Map<String, AbstractState> nodeMap, Domain d){
        String s = "digraph test {";
        
        for(String v:traceGraph.vertexSet()){
          s+=v+" [label =\""+ v+ ": "+ nodeMap.get(v).toStringShort()+"\"]\n";
        }
        for(String e:traceGraph.edgeSet()){
            s+=traceGraph.getEdgeSource(e)+"->"+traceGraph.getEdgeTarget(e)+
                    " [label=\""+ d.getAction(getAxnStrFromEdge(e)).getEffectString()+"\"];"+ "\n";
        }
        s+="}\n";
        return s;
    }

    
    public static String getNonTerminalNode(DirectedMultigraph<String, String> g, List<String> nodes){
        for (String node:nodes){
            if (g.outDegreeOf(node)>0){
                return node;
            }
        }
        return null;
    }
    
}
