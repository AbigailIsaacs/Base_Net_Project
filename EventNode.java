import java.util.ArrayList;
import java.util.Collection;

public class EventNode {
    String name ;
    ArrayList<String> outcoms;
    ArrayList<String> perents ;
    ArrayList<String> CPT;

    public EventNode(){
       this.name = "";
       perents = new ArrayList<>();
       CPT = new ArrayList<>();
       outcoms = new ArrayList<>();
    }

    public void setName(String s) {
        this.name = s;
    }

    public int getOutcomsSize(){
        return outcoms.size();
    }

    public void getOutcoms(String s) {
        this.outcoms.add(s);
    }

    public String toString(){
        return "name:" + this.name + " outcoms:" + outcoms+ " perents:" + perents + " CPT:"+ CPT;
    }

    public ArrayList<String> getPerents() {
        return perents;
    }

    public ArrayList<String> getCPT() {
        int numOfColomns = perents.size()+ outcoms.size();
        int numOfRowes =0;
        if (perents.size() == 0) {
            numOfRowes = 1;
        }
        else{
            for (int i=0; i<perents.size(); i++) {
                numOfRowes =numOfRowes *(perents.get(i).getOutcomsSize());
            }
        }
        double [][] cpt = new double[numOfRowes][numOfColomns];
        return CPT;
    }
}
