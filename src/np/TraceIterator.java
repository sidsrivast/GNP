/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package np;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedMultigraph;

/**
 *
 * @author Sid
 */
public class TraceIterator {
    private Trace t;
    private String currentNode;
    
    
    TraceIterator(Trace t1){
        t = t1;
        currentNode = "0";
    }
    

    public boolean hasNext(){
        return (t.getGraph().outDegreeOf(currentNode)>0);
    }

    
    public List<String> next()
            throws NoSuchElementException 
    {
        List<String> nextNodes = new ArrayList<String>();
        boolean nextNodeFound = false;
        
        if (!hasNext()){
            throw new NoSuchElementException(); 
        }
        
        DirectedMultigraph<String, String> graph = t.getGraph();
        nextNodes.addAll(Graphs.successorListOf(graph, currentNode));
        
        for (String node:nextNodes){
            if (graph.outDegreeOf(node)>0){
                currentNode = new String(node);
                nextNodeFound = true;
                break;
            }
        }
        
        if (!nextNodeFound){
            currentNode = nextNodes.get(0);
        }
        
        return nextNodes;
    }
}
