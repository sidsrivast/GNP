/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package np;
import java.util.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.alg.*;
/**
 *
 * @author Sid
 */
public class Trace {
    private DirectedMultigraph<String, String> traceGraph;
    private Map<String, AbstractState> nodeMap; 
    private List<Action> actionSequence;
    private Domain currentDomain;
    private int numNodes = 0;   
    
    Trace(Domain aDomain, List<String> actionStrs, ConcreteState c){
        traceGraph = new DirectedMultigraph<String, String>(String.class);
        nodeMap = new HashMap<String, AbstractState>();
        actionSequence = new ArrayList<Action>();
        currentDomain = aDomain;
        Action tempAxn;
        String axnName;
        
        for(String axnArgStr:actionStrs){
            axnName = axnArgStr.split(";")[0].trim();
            tempAxn = currentDomain.actionMap.get(axnName);
            if (tempAxn == null){
                System.out.println("Couldn't find action >>"+axnName+"<<");
                System.exit(-1);
            }
            actionSequence.add(tempAxn);
        }
        this.generateTraceGraph(c);
    }
    
    
    public DirectedMultigraph<String, String> getGraph(){
        return traceGraph;
    }
    

    public AbstractState getNodeStruc(String node){
        return nodeMap.get(node);
    }
    
    
    public String addNode(AbstractState s){
        nodeMap.put(Integer.toString(numNodes), s);
        traceGraph.addVertex(Integer.toString(numNodes));
        numNodes ++;
        return Integer.toString(numNodes-1);
    }
    
    
    public String getConsistentNode(ConcreteState c, List<String> nodes){
        AbstractState s;
        s = c.getAbstraction(currentDomain.getLandmarks());

        for (String node:nodes){
            if (s.equivalent(this.nodeMap.get(node))){
                return node;
            }
        }
        return null;
    }
    
    
    public void generateTraceGraph(ConcreteState s){
        ConcreteState concState, prevConcState = s;
        AbstractState initAbs=s.getAbstraction(currentDomain.getLandmarks());
        AbstractState prevState = initAbs;
        Set<AbstractState> nextStates;
        List<String> addedNodes= new ArrayList<String>();
        String addedNode, prevNode;
        
        prevNode = this.addNode(prevState);
        
        for (Action axn:actionSequence){
            nextStates = axn.applyAction(prevState, currentDomain.getLandmarks());

            for (AbstractState state:nextStates){
                addedNode = this.addNode(state);
                traceGraph.addEdge(prevNode, addedNode, axn.getName()+"_"+
                        prevNode + "_" + addedNode);
                addedNodes.add(addedNode);
            }
            
            concState =  axn.applyAction(prevConcState);          
            if ((prevNode = getConsistentNode(concState, addedNodes)) == null){
                System.out.println("No matching node found while applying action "+
                        axn.getName());
                //System.out.println("Previous state:");
                //System.out.println(this.nodeMap.get(prevNode).toString());
                System.out.println("Next concrete states\n");
                System.out.println(concState.toString());
                System.out.println("Next abstract states:\n");
                for (AbstractState state2:nextStates){
                    System.out.println(state2.toString()+"\n--\n");
                }
                System.exit(-1);
            }
            
            prevConcState = concState;
            prevState = this.nodeMap.get(prevNode);
            addedNodes.clear();
        }
        
    }
    
    
    public void writeTraceDot(String path){
        Utils.writeToFile(Utils.graphToDotString(traceGraph, this.nodeMap, this.currentDomain), path);
    }
    
    
    public String getTraceDot(){
        return Utils.graphToDotString(traceGraph, this.nodeMap, this.currentDomain);
    }
    
    
    public String test(String x){
        
        DirectedMultigraph<String, String> g =
            new DirectedMultigraph<String, String>(String.class);
        x="test";
        g.addVertex("1");
        g.addVertex("2");
        g.addEdge("1", "2", "e");
        g.addEdge("1", "2", "e2");
        g.addEdge("2", "1", "e2_2_1");
        System.out.println(g.toString());
        StrongConnectivityInspector gc = new StrongConnectivityInspector<String, String>(g);
        System.out.println(gc.stronglyConnectedSets().toString());
        return x;
    }
}
