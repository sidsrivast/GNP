/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package np;

import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.DirectedSubgraph;

import java.util.*;

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
        
        Utils.writeToFile(Utils.GPToDotString(gpGraph, this.gpNodeMap, this.GPDomain), path);
        
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
                // with. If none is found, *adds and returns a new node*.
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
            this.writeGraphToFile("outputs/debug.dot");
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
        System.out.println("Considering "+ gpNode1+": "+ getNodeStruc(gpNode1).toStringShort() + " -" + axn+ "-> "+gpNode2 +
                ": " + getNodeStruc(gpNode2).toStringShort());

        String addedEdge = this.addEdgeToGraph(gpNode1, gpNode2, axn);
        
        DirectedMultigraph<String, String> testGraph = (DirectedMultigraph<String, String>) this.gpGraph.clone();
                
        if (!isTerminatingGraph(getContainingSCC(testGraph, gpNode2))){
            System.out.println("Edge rejected");
            this.gpGraph.removeEdge(addedEdge);
            addedEdge = null;
        }
        System.out.println("Edge added");
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
        Set<String> edgesRemoved = null;
        

        edgesRemoved = removeUncompensatedEdges(scc);
        
        if (edgesRemoved.isEmpty()){
            return false;
        }
        
        return isTerminatingGraph(scc);
        //return true;
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
    
   
    public Set<String> removeUncompensatedEdges(DirectedSubgraph<String, String> scc){
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
        // A variable is uncompensated if either 
        // it is decremented by every action affecting it OR
        // it is incremented by every action affecting it and is not in the final interval
        Set<String> varsDecremented = new HashSet<String>(decVars.keySet());
        Set<String> varsIncremented = new HashSet<String>(incVars.keySet());
        varsDecremented.removeAll(varsIncremented);
        varsIncremented.removeAll(varsDecremented);
        
        //All vars in varsIncremented and varsDecremented have a unique interval in all nodes in the SCC.
        //compute incremented vars that are in the final interval
        String arbitNode;
        Iterator<String> sccNodeIterator = scc.vertexSet().iterator();
        Set<String> nonTerminalVars = new HashSet<String>();
        for(String var:varsIncremented){
            arbitNode = sccNodeIterator.next();
            if (this.getNodeStruc(arbitNode).getInterval(var).getUB() == -1){
                nonTerminalVars.add(var);
            }
        }
        if (!nonTerminalVars.isEmpty()){
            varsIncremented.removeAll(nonTerminalVars);
        }
        //Any edge changing a progress var can only be executed finitely many times
        Set<String> removed = new HashSet<String>();
        removed.addAll(removeEdgesAffectingVars(scc, decVars, varsDecremented));
        removed.addAll(removeEdgesAffectingVars(scc, incVars, varsIncremented));        
          
        return removed;
    }
    
    
    public Set<String>  removeEdgesAffectingVars(DirectedSubgraph<String, String> scc, 
            Map<String, Set<String>> varEdge, Set<String> varSet){
        
        Set<String> removed = new HashSet<String>();
        for(String var:varSet){
            removed.addAll(varEdge.get(var));
        }
        scc.removeAllEdges(removed);
        return removed;    
    }
    
    
    // Find the scc containing a node. 
    public DirectedSubgraph<String, String> getContainingSCC(DirectedMultigraph G, String node){
        
        StrongConnectivityInspector sci = new StrongConnectivityInspector<String, String>(G);
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
    
    
    public DirectedMultigraph<String, String> getDual(){
         DirectedMultigraph<String, String> dualGraph = new DirectedMultigraph<String, String>(String.class);
         
         for (String node:this.gpGraph.vertexSet()){
             System.out.println(node);
             String axn = "";
             Set<String> addedAxns = new HashSet<String>();
             if (!dualGraph.containsVertex(node)){
                 dualGraph.addVertex(node);}
             Set<AbstractState> possibleResults = new HashSet<AbstractState>();
             for(String edge:this.gpGraph.outgoingEdgesOf(node)){
                 System.out.println("___"+node);
                 possibleResults.add(this.gpNodeMap.get(this.gpGraph.getEdgeTarget(edge)));
             }
             
             Map<AbstractState, Map<String, Value>> diffLabels = Utils.getDiffLabels(possibleResults);
             
             for (String edge:this.gpGraph.outgoingEdgesOf(node)){
                 String targetNode = this.gpGraph.getEdgeTarget(edge);
                 dualGraph.addVertex(targetNode);
                 String axnName = Utils.getAxnNameStrFromEdge(edge, this.GPDomain);
                 String axnNodeName = axnName+node;
                 if (!addedAxns.contains(axnName)){
                     dualGraph.addVertex(axnNodeName);
                     dualGraph.addEdge(node, axnNodeName, axnNodeName);
                     addedAxns.add(axnName);
                 }
                 if (!dualGraph.containsVertex(targetNode)){
                     dualGraph.addVertex(targetNode);}
                String condition = "";
                for (String var: diffLabels.get(this.gpNodeMap.get(targetNode)).keySet()){
                    condition += " " + diffLabels.get(this.gpNodeMap.get(targetNode)).get(var).toIneqString(var);
                }  
                dualGraph.addEdge(axnNodeName, targetNode, 
                                            axnNodeName+targetNode+": "+condition);             
             }
        }
        return removeDummyStateNodes(dualGraph, this.gpGraph.vertexSet());
        //return dualGraph;
        
    }
    
    public DirectedMultigraph<String, String> removeDummyStateNodes(DirectedMultigraph<String, String> graph, Set<String> nodes){
        DirectedMultigraph<String, String> tempGraph = new DirectedMultigraph<String, String>(String.class);
        for (String node: nodes){
            for (String sourceEdge: graph.incomingEdgesOf(node)){
                for (String targetEdge: graph.outgoingEdgesOf(node)){
                    String label = sourceEdge;
                    String sourceNode = graph.getEdgeSource(sourceEdge);
                    String targetNode = graph.getEdgeTarget(targetEdge);
                    tempGraph.addVertex(sourceNode);
                    tempGraph.addVertex(targetNode);
                    tempGraph.addEdge(sourceNode, targetNode, label);
                }
            }        
        } 
        return tempGraph;
    }
    
}
