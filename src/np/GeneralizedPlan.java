/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package np;

import java.util.*;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.DirectedSubgraph;

/**
 * Stores a generalized plan. The main object is a directed multigraph
 * from the jgrapht package. Includes methods for merging a new trace
 * into the generalized plan.
 * 
 * GPDomain -- used to retrieve action names 
 *             and effects.
 * gpNodeMap -- map from nodes in the gpGraph to abstract states
 * numNodes -- number of nodes in gpGraph. Used to create new nodes.
 * startNode -- name of the starting node in gpGraph.
 * 
 * @author Sid
 */
public class GeneralizedPlan {
    private DirectedMultigraph<String, String> gpGraph;
    private Domain GPDomain;
    private Map<String, AbstractState> gpNodeMap;
    private int numNodes=0;
    private String startNode = null;
    
    GeneralizedPlan(Domain din, AbstractState initAbs){
        GPDomain = din;
        gpGraph = new DirectedMultigraph<String, String>(String.class);
        gpNodeMap = new HashMap<String, AbstractState>();
        startNode = addNode(initAbs);
        
    }
    
    
    public Action getEdgeAction(String e){
        return GPDomain.getAction(Utils.getAxnStrFromEdge(e));
    }
    

    public String getStartNode(){
        return startNode;
    }
    
    
    public AbstractState getNodeStruc(String node){
        return gpNodeMap.get(node);
    }
    
    
    public String addNode(AbstractState struc){
        AbstractState addedState = struc.getCopy();
        String addedNode = Integer.toString(numNodes);
        gpGraph.addVertex(addedNode);
        
        gpNodeMap.put(addedNode, addedState);
        
        numNodes++;
        return addedNode;
    }
    
    
    public String addEdgeToGraph(String v1, String v2, String axn){
        String label = axn+"_"+v1+"_"+ v2;
        this.gpGraph.addEdge(v1, v2, label);
        return label;
    }
            
    
    public void writeGraphToFile(String path){
        
        Utils.writeToFile(Utils.graphToDotString(gpGraph, this.gpNodeMap, this.GPDomain), path);
        
    }
    
    
    /*
     * Merge an input trace with this generaplized plan.
     */
    
    public void mergeTrace(Trace t, String attachPoint){
        TraceIterator ti = new TraceIterator(t);
        String gpTargetNode, nextTraceNode, currentTraceNode, currentGPNode,  axn, nextGPNode = null;
        List<String> nextNodes;
        
        currentTraceNode = "0";
        currentGPNode = attachPoint;
        
        //Iterate over actions in the trace
        while (ti.hasNext()){
            // get the next possible nodes in the trace
            nextNodes = ti.next();
            axn  = Utils.getAxnStrFromEdge(t.getGraph().getEdge(
                    currentTraceNode, nextNodes.get(0)));

            
            nextTraceNode = Utils.getNonTerminalNode(t.getGraph(), nextNodes);
            nextGPNode = null;
            // Compute the next node in this gp that subsumes the non-terminal
            // trace node as gpTargetNode. We only follow non-terminal nodes. 
            // See tracing algorithm in ICAPS-11 for example.
            for (String nextTNode:nextNodes){
                // add an edge from the current gp node to the next non-terminal
                // node in the trace. Include edge searches for a node to merge
                // with. If none is found, adds and returns a new node.
                gpTargetNode = this.includeEdge(t, axn, currentTraceNode, nextTNode, currentGPNode);
                if (nextTraceNode != null && nextTNode.equals(nextTraceNode)){
                    nextGPNode = gpTargetNode;
                }
            }
            
            // The non-terminal node in the trace becomes the new currentTraceNode
            // Gp node becomes the recently added node.
            if (nextTraceNode!= null){
                currentTraceNode = nextTraceNode;
                if (nextGPNode!= null){
                    currentGPNode = nextGPNode;
                }
                else{
                    System.out.println("Error: next GP node not found");
                    System.exit(-1);
                }
            }
        }
    }
    
    
    /*
     * Includes an edge gpNode1, tnode2 from the trace. This amounts to either 
     * adding a new node to the gp and adding an edge to it from gpNode1 or
     * finding an existing gp node that subsumes tnode2 and adding the edge to
     * that node -- provided that this edge is safe to add.
     */
    public String includeEdge(Trace t, String axn, String tnode1, String tnode2, 
            String gpNode1){
        boolean safeMatchFound = false;
        Set<String> gpNodes = gpGraph.vertexSet();
        List<String> subsumingNodes = new ArrayList<String>();
        String addedEdge = null;
        String addedNode = null;
        
        AbstractState gpOriginState = gpNodeMap.get(gpNode1);
        AbstractState tOriginState = t.getNodeStruc(tnode1);
        
        if (!gpOriginState.equivalent(tOriginState)){
            System.out.println(gpNodeMap.get(gpNode1).toString());
            System.out.println(t.getNodeStruc(tnode1).toString());
            System.out.println("Error: attachment points not consistent");
            System.exit(-1);
        }
        
        AbstractState strucToAdd = t.getNodeStruc(tnode2);
        
        for (String gpNode:gpNodes){
           if (gpNodeMap.get(gpNode).equivalent(strucToAdd)) {
               subsumingNodes.add(gpNode);
           }
        }
        
        //TBD: coverage maximizing/backtracking selection of a subsuming node
        
        // Get list of all nodes that subsume this one.
        //System.out.println("consideringg nodes = "+subsumingNodes.toString());
        for (String gpNode2:subsumingNodes){
            // if adding an edge to gpNode2 is safe, do so
            addedEdge = addIfSafe(gpNode1, axn, gpNode2, strucToAdd);
            if (addedEdge!= null){
                return this.gpGraph.getEdgeTarget(addedEdge);
            }
        }
        
        if (addedEdge == null){
            // if an edge was not added, create a new node and add an edge
            // to it.
            addedNode = this.addNode(strucToAdd);
            addedEdge = this.addEdgeToGraph(gpNode1, addedNode, axn);
        }
        
        return addedNode;
    }
        
    
    public String addIfSafe(String gpNode1, String axn, String gpNode2, 
            AbstractState struc){
       //-1 System.out.println("Considering "+ gpNode1+" -> "+gpNode2);

        String addedEdge = this.addEdgeToGraph(gpNode1, gpNode2, axn);
        
        if (!isTerminatingGraph(getContainingSCC(gpNode2))){
            System.out.println("Edge rejected");
            this.gpGraph.removeEdge(addedEdge);
            addedEdge = null;
        }
        
        return addedEdge;
    }
    
    // isTerminatingGraph and isTerminatingSCC implement the sieve algorithm
    // See AAAI-11
    public boolean isTerminatingGraph(DirectedSubgraph<String, String> inputGraph){
        if (inputGraph == null){
            return true;
        }
        StrongConnectivityInspector sci = new StrongConnectivityInspector<String, String>(inputGraph);
        List<DirectedSubgraph<String,String>> sccs = sci.stronglyConnectedSubgraphs();
        
        for (DirectedSubgraph<String, String> scc: sccs){
            if (scc.edgeSet().size()>0 && !isTerminatingSCC(scc))
                return false;
        }
                
        return true;
    }
    
    
    public boolean isTerminatingSCC(DirectedSubgraph<String, String> scc){
        boolean edgesRemoved = false;
        

        edgesRemoved = removeUncompensatedEdges(scc);
        
        if (!edgesRemoved){
            return false;
        }
        
        //return isTerminatingGraph(scc);
        return true;
    }
    
    // Find an edge that decreases a variable that no other edge in this scc
    // increases. Remove this edge. Deprecated in favor of removeUncompensatedEdges
    public boolean removeEdgeWiseUncompensatedEdges(DirectedSubgraph<String, String> scc){
        boolean edgesRemoved = false;
        Map<String, Set<String>> incVars = new HashMap<String, Set<String>>();
        Map<String, Set<String>> decVars = new HashMap<String, Set<String>>();
        Set<String> uncompensatedVars = new HashSet<String>();
        
        //compute the increased and decreased var sets
        for(String edge:scc.edgeSet()){
            Action a = this.getEdgeAction(edge);
            for(String var:a.getDecVars()){
                if (!decVars.containsKey(var)){
                    decVars.put(var, new HashSet<String>());
                }
                decVars.get(var).add(edge);
            }
            for(String var:a.getIncVars()){
                if (!incVars.containsKey(var)){
                    incVars.put(var, new HashSet<String>());
                }
                incVars.get(var).add(edge);
            }
        }
        
        // Get the set of uncompensate vars
        Set<String> varsDecremented = new HashSet<String>(decVars.keySet());
        Set<String> varsIncremented = new HashSet<String>(incVars.keySet());
        varsDecremented.removeAll(varsIncremented);
        uncompensatedVars = varsDecremented;
        
        // Delete edges decreasing uncompensated vars
        for (String var:uncompensatedVars){
            for(String edge:decVars.get(var)){
                scc.removeEdge(edge);
                edgesRemoved = true;
            }
        }
        return edgesRemoved;
    }
    
   
    public boolean removeUncompensatedEdges(DirectedSubgraph<String, String> scc){
        boolean edgesRemoved = false;
        Map<String, Set<String>> incVars = new HashMap<String, Set<String>>();
        Map<String, Set<String>> decVars = new HashMap<String, Set<String>>();
        
        //compute the increased and decreased var sets
        for(String edge:scc.edgeSet()){
            Action a = this.getEdgeAction(edge);
            for(String var:a.getDecVars()){
                if (!decVars.containsKey(var)){
                    decVars.put(var, new HashSet<String>());
                }
                decVars.get(var).add(edge);
            }
            for(String var:a.getIncVars()){
                if (!incVars.containsKey(var)){
                    incVars.put(var, new HashSet<String>());
                }
                incVars.get(var).add(edge);
            }
        }
        
        // Get the set of uncompensated vars
        // A variable is un-compensated if either 
        // it is decremented by every action affecting it OR
        // it is incremented by every action affecting it
        Set<String> varsDecremented = new HashSet<String>(decVars.keySet());
        Set<String> varsIncremented = new HashSet<String>(incVars.keySet());
        varsDecremented.removeAll(varsIncremented);
        varsIncremented.removeAll(varsDecremented);
        
        //The loop must terminate if either it has uncompensated decrementing variables or
        // it has uncompensated incremented variables for which any node corresponds to a 
        // non-max interval
        if (!varsDecremented.isEmpty()){
           // Set<String> toRemove = scc.vertexSet();
           // scc.removeAllVertices(toRemove);
            edgesRemoved = true;
        }
        
        if (!edgesRemoved){
            AbstractState s;
            String arbitNode;
            Iterator<String> sccNodeIterator;
            for (Iterator<String> it = varsIncremented.iterator(); it.hasNext();) {
                String var = it.next();
                sccNodeIterator = scc.vertexSet().iterator();
                if (sccNodeIterator.hasNext()){
                    arbitNode = sccNodeIterator.next();
                    if (this.getNodeStruc(arbitNode).getInterval(var).getUB() != -1){
                        //Set<String> toRemove = scc.vertexSet();
                        //scc.removeAllVertices(toRemove);
                        edgesRemoved = true;
                        break;
                    }          
                }
            }
        }
          
        return edgesRemoved;
    }
    
    
    // Find the scc containing a node. 
    public DirectedSubgraph<String, String> getContainingSCC(String node){
        
        StrongConnectivityInspector sci = new StrongConnectivityInspector<String, String>(this.gpGraph);
        List<DirectedSubgraph<String,String>> sccs = sci.stronglyConnectedSubgraphs();
        
        for (DirectedSubgraph<String, String> scc:sccs){
            if (scc.containsVertex(node) && scc.edgeSet().size()>0){
                return scc;
            }
        }
        return null;
    }

    
    // get terminal non-goal, or open nodes
    
    public Set<String> getOpenNodes(){
        Set<String> openNodes = new HashSet<String>();
        
        for(String node:this.gpGraph.vertexSet()){
            if (gpGraph.outDegreeOf(node) == 0 && !this.GPDomain.goalCondition.satisfied(
                this.gpNodeMap.get(node))){
                openNodes.add(node);
            }
        }
        
        return openNodes;
    }
    
    
    
}
