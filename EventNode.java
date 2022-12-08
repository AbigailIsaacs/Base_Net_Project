import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashMap;

/**
 * class members -
 * name - is the name of the event
 * outcomes - the values (for example {T=0} {F=1} {V3=2})
 * parents - the parents of an eventNode (the <GIVEN> tag)
 * CPT - the cpt table as an array list
 * cptTable - is the cpt as a matrix
 */
public class EventNode {
    String name ;
    HashMap<String, Integer> outcomes;
    ArrayList<EventNode> parents ;
    ArrayList<Double> CPT;
    double cptTable[][];


    /**
     * constructor
     */
    public EventNode(){
       this.name = "";
        parents = new ArrayList<>();
        CPT = new ArrayList<>();
        outcomes = new HashMap<String, Integer>();
    }

    /**
     *
     * @param key
     * @return the value of the outcome (for example {T=0} {F=1} {V3=2})
     */
    public int hashValue(String key){

        return outcomes.get(key);
    }
    public void setName(String s) {
        this.name = s;
    }
    public String getName() {
        return this.name;
    }

    public HashMap<String, Integer> getOutcomes() {
        return outcomes;

    }

    public int getOutcomesSize(){
        return outcomes.size();
    }

    public String toString(){
        String parentsNames ="[";
        for (int i=0; i<(parents.size());i++){
            parentsNames = parentsNames +parents.get(i).getName()+", ";
        }
        parentsNames =parentsNames+ "]";
        return "name:" + this.name + " outcomes:" + outcomes+ " parents:" + parentsNames + " CPT:"+ CPT;
    }

    public ArrayList<EventNode> getParents() {
        return parents;
    }
    public ArrayList<Double> getCPT(){
        return CPT;
    }
    public double [][] getCptTable(){
        return cptTable;
    }

    /**
     * creates a CPT table
     * @return cptTable
     */
    public double [][] createCPT() {
        int numOfColumns = outcomes.size();
        int numOfRowes = 1;
        for (int i=0; i<parents.size(); i++) {
            numOfRowes =numOfRowes *(parents.get(i).getOutcomesSize());
        }
        double [][] cpt = new double[numOfRowes][numOfColumns];
        int index = 0;
            for (int i = 0 ; i< cpt.length; i++){
                for (int j=0; j< cpt[0].length;j++){
                    cpt[i][j] = CPT.get(index++);
                }
            }
        cptTable = cpt ;
        return cpt;
    }

}
