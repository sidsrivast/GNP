/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package np;

import org.jgrapht.graph.DirectedMultigraph;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
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
        String[] eSegs = e.split("_");
        String name = "";
        
        for(int i=0; i<eSegs.length-2; i++){
            name += eSegs[i];
            if (i<eSegs.length - 3){
                name += "_";
            }
        }
        
        return name;
    }
   
    
    public static String getAxnNameStrFromEdge(String e, Domain d){
        return d.getAction(getAxnStrFromEdge(e)).getName();
    }
    
    /*
     * Rtturn the graph (from package jgrapht) in dot format. Can be plotted 
     * using graphviz.
     * 
     */
    public static String GPToDotString(DirectedMultigraph<String, String> traceGraph, 
            Map<String, AbstractState> nodeMap, Domain d){
        String s = "digraph test {";
        
        for(String v:traceGraph.vertexSet()){
          s+=v+" [label =\""+ v+ ": "+ nodeMap.get(v).toStringShort()+"\"]\n";
        }
        for(String e:traceGraph.edgeSet()){
            s+=traceGraph.getEdgeSource(e)+"->"+traceGraph.getEdgeTarget(e)+
                    " [label=\""+ d.getAction(getAxnStrFromEdge(e)).getName()+"\"];"+ "\n";
        }
        s+="}\n";
        return s;
    }

    
    public static String graphToDotString(DirectedMultigraph<String, String> graph){
       String s = "digraph test {";
        
        for(String v:graph.vertexSet()){
          s+=v+" [label =\""+ v+ "\"]\n";
        }
        for(String e:graph.edgeSet()){
            s+=graph.getEdgeSource(e)+"->"+graph.getEdgeTarget(e)+
                    " [label=\""+ e +"\"];"+ "\n";
        }
        s+="}\n";
        return s;
    }
    
    public static void writeGraphToDotFile(DirectedMultigraph<String, String> graph, String path){
        writeToFile(graphToDotString(graph), path);
    }
    
    
    public static String getNonTerminalNode(DirectedMultigraph<String, String> g, List<String> nodes){
        for (String node:nodes){
            if (g.outDegreeOf(node)>0){
                return node;
            }
        }
        return null;
    }
    
    
        public static Set<String> getMatchingVars(Set<AbstractState> stateSet){
        AbstractState prevState=null;
        Set<String> matchingVars = new HashSet<String>();
        for (AbstractState currentState : stateSet){
            if (prevState == null){
                prevState = currentState;
                matchingVars.addAll(currentState.getVars());
                matchingVars.addAll(currentState.getBoolVars());
            }
            Set<String> diffs = new HashSet<String>();
            for (String var:matchingVars){
                if (currentState.getVars().contains(var)){
                    if (var.equals("clothesInBasket")){
                        System.out.println();
                    }
                    if (!prevState.getInterval(var).equals(currentState.getInterval(var))){
                        diffs.add(var);
                    }
                }else if (currentState.getBoolVars().contains(var)) {
                    if (prevState.getBoolValue(var) != currentState.getBoolValue(var)){
                        diffs.add(var);
                    }
                }
            }
            boolean removeAll = matchingVars.removeAll(diffs);
            prevState = currentState;
        }
        return matchingVars;
    }
    
    
    public static Map<AbstractState,Map<String, Value>> getDiffLabels(Set<AbstractState> stateSet){
        Set<String> vars = getMatchingVars(stateSet);
        Map<AbstractState, Map<String, Value>> diffLabels = new HashMap<AbstractState, Map<String, Value>>();
        
        for (AbstractState s: stateSet){
            diffLabels.put(s, s.getStrAllBut(vars));
        }
        return diffLabels;
    }
    
}
