import java.util.ArrayList;
import java.util.Arrays;

public class Factor {

    double factor [];
    ArrayList<String>  factor_name;
    BaseNet baseNet ;

    public Factor(EventNode event, BaseNet baseNet){
        factor = createFactor(event);
        factor_name = createFactorName(event);
        this.baseNet =baseNet;
    }

    public Factor(ArrayList<String> name,double[] factorTable, BaseNet baseNet){
        setFactor(factorTable);
        setFactorName(name);
        this.baseNet =baseNet;
    }
    public void setFactorName (ArrayList<String> factorName){

        factor_name = factorName;
    }
    public void setFactor (double[] factorTable){
        factor = factorTable;
    }
    public double[] createFactor(EventNode event){
        int rows = event.getCptTable().length * event.getOutcomesSize() ;
        double [] factor = new double[rows];
        int counter =0;
        for(int i=0;i<event.getCptTable().length;i++){

            for (int j=0;j<event.getCptTable()[0].length;j++){
                factor[counter++] = event.getCptTable()[i][j];
            }
        }
        return factor;
    }
    public ArrayList<String> createFactorName(EventNode event){
        ArrayList<String> factor_name= new ArrayList<String>();
        for (int i=0;i<event.getParents().size();i++) {

            factor_name.add(event.getParents().get(i).getName()); //P(B=T|J=T,M=T),1
        }
        factor_name.add(event.getName());
        return factor_name;
    }

    public ArrayList<String> getFactorName(){

        return factor_name;
    }
    public double[] getFactor(){

        return factor;
    }

    public double[] eliminateEvidence(EventNode evidence, int component){

        int rows = this.factor.length/evidence.getOutcomesSize();
        // if rows =1 then null
        double [] newFactor = new double[rows];
        int jumps = this.factor.length;
        for (int i=0;i<factor_name.size();i++){
            if(evidence.getName().equals(factor_name.get(i))){
                jumps = jumps/baseNet.hashValue(factor_name.get(i)).getOutcomes().size();
                break;
            }
            else{
                jumps= jumps/baseNet.hashValue(factor_name.get(i)).getOutcomes().size();
            }
        }
        int indexNew=0;
        for (int i=component*jumps;i<factor.length;i=i+(jumps*(evidence.getOutcomes().size()-1))){
            for (int j=0;j<jumps;j++){
                newFactor[indexNew++]= factor[i++];
            }

        }

        factor = newFactor;
        return factor;
    }
    public ArrayList<String> deleteEventName(String event){
        for (int i=0;i<factor_name.size();i++){
            if (event.equals(factor_name.get(i))){
                factor_name.remove(i);
            }
        }
        return factor_name;
    }



    public String toString(){

        return "name:" + this.factor_name + " table:" + Arrays.toString(factor);
    }
}
