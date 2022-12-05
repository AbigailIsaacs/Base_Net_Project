import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashMap;

public class EventNode {
    String name ;
    HashMap<String, Integer> outcoms;
    ArrayList<EventNode> perents ;
    ArrayList<Double> CPT;
    double cptTable[][];

    public EventNode(){
       this.name = "";
       perents = new ArrayList<>();
       CPT = new ArrayList<>();
       outcoms = new HashMap<String, Integer>();
    }
    public int hashValue(String key){
        return outcoms.get(key);
    }
    public void setName(String s) {
        this.name = s;
    }
    public String getName() {
        return this.name;
    }

    public HashMap<String, Integer> getOutcoms() {
        return outcoms;
    }

    public int getOutcomsSize(){
        return outcoms.size();
    }

    public HashMap<String, Integer> GetOutcoms() {
        return this.outcoms;
    }

    public String toString(){
        String perentsNames ="[";
        for (int i=0; i<(perents.size());i++){
            perentsNames = perentsNames +perents.get(i).getName()+", ";
        }
        perentsNames =perentsNames+ "]";
        return "name:" + this.name + " outcoms:" + outcoms+ " perents:" + perentsNames + " CPT:"+ CPT;
    }

    public ArrayList<EventNode> getPerents() {
        return perents;
    }
    public ArrayList<Double> getCPT(){
        return CPT;
    }
    public double [][] getCptTable(){
        return cptTable;
    }
    public double [][] createCPT() {
        int numOfColomns = outcoms.size();
        int numOfRowes = 1;
        for (int i=0; i<perents.size(); i++) {
            numOfRowes =numOfRowes *(perents.get(i).getOutcomsSize());
        }
        double [][] cpt = new double[numOfRowes][numOfColomns];
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
