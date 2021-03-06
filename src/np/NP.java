/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package np;

import java.io.BufferedReader;
import java.io.InputStreamReader;
        
/**
 *
 * @author siddharth
 * 
 * The main class.
 */
public class NP {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws java.io.IOException, InterruptedException{
        
        System.out.print("Enter input filename (in GNPFinal/inputs/): ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String inputFile = "quad_hall.txt";
        //String inputFile = "laundry_basket.txt";
//        try {
//            inputFile = br.readLine();
//        } catch (IOException e) {
//            System.out.println("Error!");
//            System.exit(1);
//        }
        
        Domain myDomain = NPinput.getDomainFromFile("inputs/"+inputFile);

        System.out.format("%s\n\n",myDomain.toString());
        HybridSearch searchObj = new HybridSearch(myDomain);
        searchObj.doSearch("outputs/output");
    }
}
