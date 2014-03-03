/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package np;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * The incremental search and merge process. Main methods used are
 * from the class GeneralizedPlan.
 * 
 * @author Sid
 */
public class HybridSearch {

    private Domain myDomain;
    private GeneralizedPlan gp;
    
    HybridSearch(Domain inDomain){
        myDomain = inDomain;    
        gp = new GeneralizedPlan(inDomain, inDomain.initialState);
    }
    
    
    /*
     * Starting with an empty generalized plan initialized with the 
     * initial abstract state, incrementally ask user for a node to expand,
     * create a problem instance, solve it and merge the result.
     * 
     * Outputs are stored in the form of dot files in the project directory (NP/).
     * Parameter outputPrefix is the common prefix.
     */
    public void doSearch(String outputPrefix)throws java.io.IOException, InterruptedException
    {
        String selectedNode = "0";
        ConcreteState c = null;
        int i;
        myDomain.toDomainPDDLFile("inputs/testD.pddl");
        long startTime = System.currentTimeMillis();
        long duration = 0;
        
        for(i=0;i<25;i++){
            c = gp.getNodeStruc(selectedNode).getInstance(0, 5);
            System.out.print(c.toString());
            System.out.println("\n\n");
            myDomain.toProblemPDDLFile("inputs/testP.pddl", c);
     
            Planner myPlanner = new Planner("./ff", "-p inputs/ -o testD.pddl -f testP.pddl");
            Map result = myPlanner.invoke(10);
            int retVal = (Integer) result.get(1);
            String outputStr = (String) result.get(2);
    
            PlannerOutput currentOutput = new FFOutput(outputStr);
            Trace t = new Trace(myDomain, currentOutput.getPlanActions(), c);

            gp.mergeTrace(t, selectedNode);
            duration += System.currentTimeMillis()-startTime;

            String ofile = outputPrefix + Integer.toString(i) + ".dot";
            
            System.out.println("\nWriting generalized plan in "+ ofile);
            gp.writeGraphToFile(outputPrefix + Integer.toString(i)+".dot");

            
            selectedNode = getSelectedNodeAuto();
            if (selectedNode.equals("-1")){
                System.out.format("Total time taken: %f\n", duration*1.0/1000);
                System.exit(0);
            }
            startTime = System.currentTimeMillis();
            
        }
    }

    
    public String getSelectedNode(){
        System.out.print("Open nodes: ");
        System.out.println(this.gp.getOpenNodes().toString());
        System.out.print("Enter node to expand (-1 to quit): ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String selectedNode = null;
        try {
            selectedNode = br.readLine();
        } catch (IOException e) {
            System.out.println("Error!");
            System.exit(1);
        }
        
        return selectedNode;
    }
    
    
    public String getSelectedNodeAuto(){
    
        if (this.gp.getOpenNodes().isEmpty()){
            return "-1";
        }
    
        return this.gp.getOpenNodes().iterator().next();
    }
    
}
