import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;

/**
 * class members -
 * events - all the events in the Bayesian network
 * name_XML
 */
public class BaseNet {
    ArrayList<EventNode> events ;// מערך של כל המאורעות
    String name_XML ;
    HashMap<String, EventNode> hashEvents = new HashMap<>();

    /**
     * constructor -
     * initialize events by using readXML() function
     */
    public BaseNet(String nameXML) throws ParserConfigurationException, IOException, SAXException {
        this.name_XML = nameXML;
        events = readXML(nameXML);
    }


    public ArrayList<EventNode> getEvents(){
        return events;
    }
    public ArrayList<EventNode> readXML(String nameXML) throws ParserConfigurationException, IOException, SAXException {
        File file = new File(nameXML);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        document.getDocumentElement().normalize();
        ArrayList<EventNode> events = new ArrayList<>();
        // קולטים את המאורעות
        NodeList varList = document.getElementsByTagName("VARIABLE");
        for (int i = 0; i < varList.getLength(); i++) // מעבר על כל המאורעות
        {
            Node var = varList.item(i);
            if (var.getNodeType() == Node.ELEMENT_NODE) {
                Element varElement = (Element) var;
                String s = varElement.getElementsByTagName("NAME").item(0).getTextContent();
                EventNode e = new EventNode(); // יוצרת נואוד חדש למאורע
                e.setName(s);
                events.add(e);
                // HashMap<String, Integer> outcomes = new HashMap<String, Integer>();
                for (int j = 0; j < varElement.getElementsByTagName("OUTCOME").getLength(); j++) {
                    String outcome = varElement.getElementsByTagName("OUTCOME").item(j).getTextContent();
                    e.getOutcomes().put(outcome, j);//  מוסיפה לנואוד את האוטוקאם

                }
            }
        }
// עד פה קליטת כל המאורעות ויצירת נואוד חדש לכל מאורע והשמת כל המאורעות במערך דינמי
        NodeList defList = document.getElementsByTagName("DEFINITION");
        for (int i = 0; i < defList.getLength(); i++) // מעבר על כל הטבלאות
        {
            Node def = defList.item(i);
            if (def.getNodeType() == Node.ELEMENT_NODE) {
                Element defElement = (Element) def;
                for (int j = 0; j < defElement.getElementsByTagName("GIVEN").getLength(); j++) {
                    String name = defElement.getElementsByTagName("GIVEN").item(j).getTextContent();
                    // מעבר על המערך של המאועות עד למציאת האבא והוספתו למערך האבות של המשתנה
                    // finds the parent eventNode
                    for (int k = 0; k < events.size(); k++) {
                        if (events.get(k).getName().equals(name)) {
                            events.get(i).getParents().add(events.get(k)); // adding the parents
                        }
                    }
                }
                String numbers = defElement.getElementsByTagName("TABLE").item(0).getTextContent();
                StringTokenizer cptNumbers = new StringTokenizer(numbers);
                while (cptNumbers.hasMoreTokens()) {
                    double num = Double.parseDouble(cptNumbers.nextToken());
                    events.get(i).getCPT().add(num);// adding the cpt numbers
                }
            }
        }
        for (int i=0 ; i<events.size();i++){
            events.get(i).createCPT();
        }
        for (int i=0;i<events.size();i++){
            hashEvents.put(events.get(i).name, events.get(i));
        }


        return events;

    }
    //****************** calculateQustion
    public double [] function1 (ArrayList<EventNode> query_and_evedent, ArrayList<Integer> components) {
        double ans=0;
        int numOfPlus =0;
        double [] toReturn = new double [3];
        double [] fromCalc;
        int numMultiplcations =0;
        ArrayList<EventNode> allNodes = appendNodes(query_and_evedent,getHidden(query_and_evedent));
        int[][] options = options(query_and_evedent); // מחזיר מערך עם כל האופציות

        for(int i=0;i<options.length;i++) {//מעבר על כל השורות
            int [] arr = new int[options[0].length]; // פותחת מערך בגודל של שורה
            for (int j=0;j<options[0].length;j++){ // מעתיקה את השורה במערך
                arr[j] = options[i][j];
            }
            ArrayList<Integer> allComponents = appendComponents(components,arr);

            fromCalc =calc(allNodes, allComponents); // שולח לפונקציה חישוב
            ans+= fromCalc[0];
            numOfPlus++;
            numMultiplcations += fromCalc[1];
        }
        toReturn[0] = ans;
        toReturn[1] = numOfPlus-1;
        toReturn [2] = numMultiplcations;
        return toReturn;
    }

    public double [] function2 (ArrayList<EventNode> query_and_evedent, ArrayList<Integer> components){
        double [] toReturn = new double [3];
        ArrayList<Factor> factors=  createAllFactors();
        elimnateAllEvedence(query_and_evedent,components,factors);
        ArrayList<EventNode> hidden = getHidden(query_and_evedent);
        hidden.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
       // for (int i=0; i<hidden.size();i++){
            ArrayList<Factor> factorEvent = cerateArrFactorsForHidden(hidden.get(2),factors);
           // for (int j=0; j<factorEvent.size();j++){
               // join(factorEvent.get(j),factorEvent.get(j+1));
        System.out.println("F1 -"+ factorEvent.get(0));
        System.out.println("F2 -"+ factorEvent.get(1));
            join(factorEvent.get(0),factorEvent.get(1));

           // }
       // }

        return toReturn;
    }
    public void join (Factor factor1,Factor factor2) {
        ArrayList<EventNode> allNodesForJoin = new ArrayList<>();
        ArrayList<String> factor_name = new ArrayList<>();

        for (int i = 0; i < factor1.factor_name.size(); i++) {
            allNodesForJoin.add(hashValue(factor1.factor_name.get(i)));
            factor_name.add(hashValue(factor1.factor_name.get(i)).name);
        }
        boolean b = true;
        for (int i = 0; i < factor2.factor_name.size(); i++) {
            b = true;
            for (int j = 0; j < factor1.factor_name.size(); j++) {
                if (factor2.factor_name.get(i).equals(factor1.factor_name.get(j))) {
                    b= false;
                }
            }
            if(b) {
                allNodesForJoin.add(hashValue(factor2.factor_name.get(i)));
                factor_name.add(hashValue(factor2.factor_name.get(i)).name);
            }
        }
        System.out.println("all nodes "+allNodesForJoin);
        int[][] options = optionsForF2(allNodesForJoin);
        System.out.println("options length "+options.length);
        ArrayList<Integer> F1Index = new ArrayList<>();
        ArrayList<Integer> F2Index = new ArrayList<>();

        ArrayList<EventNode> nodesF1 = new ArrayList<>();
        ArrayList<EventNode> nodesF2 = new ArrayList<>();
        for (int i = 0; i < allNodesForJoin.size(); i++) {
            for (int j = 0; j < factor1.factor_name.size(); j++) {
                if (factor1.factor_name.get(j).equals(allNodesForJoin.get(i).getName())) {
                    F1Index.add(i);
                    nodesF1.add(allNodesForJoin.get(i));
                }
            }
            for (int j = 0; j < factor2.factor_name.size(); j++) {
                if (factor2.factor_name.get(j).equals(allNodesForJoin.get(i).getName())) {
                    F2Index.add(i);
                    nodesF2.add(allNodesForJoin.get(i));
                }
            }
        }
        double NewFactor[] = new double[options.length];
        int [] componentsF1 = new int[F1Index.size()];
        int [] componentsF2 = new int[F2Index.size()];
        for (int i = 0; i < options.length; i++) {
            for (int j=0;j<options[0].length;j++) {
                for (int k = 0; k < F1Index.size(); k++) {
                    if (j == F1Index.get(k)) { // כלומר האם האינקס שייך לF1
                        componentsF1[j] = options[i][j];

                    }
                }
                for (int k = 0; k < F2Index.size(); k++) {
                    if (j == F2Index.get(k)) { // כלומר האם האינקס שייך לF1
                        componentsF2[j] = options[i][j];
                    }
                }
            }
            NewFactor[i] = (getTA(factor1,componentsF1,nodesF1)) * (getTA(factor2,componentsF2,nodesF2));
        }
        System.out.println("joined - " + Arrays.toString(NewFactor));
    }
    public double getTA(Factor factor,int[] components,ArrayList<EventNode> nodesF){
        int ans =0;
        double mone = factor.factor.length;
        double div =1;
        ArrayList<EventNode> NewnodesF = new ArrayList<>();
        int [] newComponents = new int[components.length];
        //nodesF סידור נכון של
        for (int i = 0; i <factor.factor_name.size(); i++) {
            for (int j = 0; j < nodesF.size(); j++) {
                if(factor.factor_name.get(i).equals(nodesF.get(j).getName())){
                    newComponents[i] = components[j];
                }
            }
        }
        for (int i = 0; i < nodesF.size(); i++) {
            div =div * (hashValue(factor.factor_name.get(i)).getOutcomes().size());
            ans += newComponents[i]*mone/div;
        }
        return factor.factor[ans];
    }
    public ArrayList<Factor> cerateArrFactorsForHidden(EventNode hidden,ArrayList<Factor>factors){
        ArrayList<Factor> factorEvent = new ArrayList<>()  ;
        for (int i=0; i<factors.size();i++){
            for (int j=0;j<factors.get(i).factor_name.size();j++){
                if(factors.get(i).factor_name.get(j).equals(hidden.getName())){
                    factorEvent.add(factors.get(i));
                    factors.remove(i);
                    i--;
                    break;
                }
            }

        }

        Collections.sort(factorEvent, Comparator.comparingInt(p -> p.getFactor().length));

        return factorEvent;
    }
    public void elimnateAllEvedence(ArrayList<EventNode> query_and_evedent, ArrayList<Integer> components,ArrayList<Factor>factors){
        for(int i=1; i<query_and_evedent.size();i++){ //עובר על כל משתני האווידנס
            for (int j=0;j<factors.size();j++){ // עובר על כל הפקטורים
                for (int k=0;k<factors.get(j).factor_name.size();k++){ //עובר על הנואודים שנמצאיםם בפקטור
                    if (factors.get(j).factor_name.get(k).equals(query_and_evedent.get(i).name)){
                        double[] newFactor = factors.get(j).eliminateEvidence(query_and_evedent.get(i),components.get(i));
                        ArrayList<String> newName= factors.get(j).deleteEventName(query_and_evedent.get(i).name);
                        factors.get(j).factor = newFactor;
                        break;
                    }
                }
            }
        }

    }
    public ArrayList<Factor> createAllFactors(){
        ArrayList<Factor> factors = new ArrayList();
        for (int i=0;i<events.size();i++) {
            factors.add(new Factor(this.events.get(i), this));
        }
        return factors;
    }
    /* a formula to find the cell in the table -
     the component of event1 * num of rows in the query table/number of components that event1 has +
     the component of event2 * num of rows in the query table/number of components that event2 has *number of components that event2 has +
     the component of event3 * num of rows in the query table/number of components that event1 * event2 * event3......
    */

    /**
     * @param allNodes - all nods in the Bayesian  network
     * @param allComponents - the components that we are cheking for
     * @return arr[0] is the answer to the question P(A,B,C,-D-E)
     *         arr[1] number of multiplications
     */

    public double [] calc (ArrayList<EventNode> allNodes,ArrayList<Integer> allComponents) {
        double [] toReturn = new double [2]; //
        double ans =1;
        int numMultiplcations  =0;
        for(int i=0; i<allNodes.size();i++){ // מעבר על כל מאורע
            int colum = allComponents.get(i);
            int row =0 ;
            int divide =1;
            for (int j=0 ; j<allNodes.get(i).getParents().size();j++){ //   מעבר על כל ההורים של המאורע
                EventNode parent = allNodes.get(i).getParents().get(j); //
                int parentComponent =0;
                for(int k=0;k<allNodes.size();k++){ // to find the index of the parent
                    if(parent.getName().equals(allNodes.get(k).getName())){
                        parentComponent = allComponents.get(k);
                        break;
                    }
                }
                divide= divide*(parent.getOutcomesSize());
                int rowsOfQuery = allNodes.get(i).getCptTable().length ;
                row = row + (parentComponent*(rowsOfQuery/divide)) ;
            }
            double tableTA = (allNodes.get(i).getCptTable())[row][colum];
            ans = ans *tableTA ;
            numMultiplcations++;
        }
        toReturn[0] = ans;
        toReturn[1]= numMultiplcations-1 ;
        return toReturn;
    }


//הפונקציה מחשבת את כל האופציות של המשתנים החבואים ומחזירה אותם במטריצה
    private int getNumOfOptions(ArrayList<EventNode> hidden) {
        int sum =1;
        for (int i=0; i<hidden.size();i++){
            sum= sum*(hidden.get(i).getOutcomesSize());
        }
        return sum;
    }
    public ArrayList<EventNode> getHidden(ArrayList<EventNode> query_and_evedent){
        ArrayList<EventNode> hidden = new ArrayList<>();
        boolean b = true;
        for(int i =0; i< this.events.size(); i++){
            for (int j=0 ; j<query_and_evedent.size();j++) {
                if (events.get(i).getName().equals(query_and_evedent.get(j).getName())) {
                    b = false;
                }
            }
            if (b){
                hidden.add(events.get(i));
            }
            b=true;
        }

        return hidden;
    }

    public int[][] options (ArrayList<EventNode> query_and_evedent){
        ArrayList<EventNode> hidden = getHidden(query_and_evedent) ;
        int NumOfOptions = getNumOfOptions(hidden); //מחזיר את כמות הp() שצריך לעשות עם כל הקומבינציות של המשתנים החבואים
        int[][] arr_options = new int[NumOfOptions][hidden.size()];
        // puting data in the matrix
        int jumps = NumOfOptions;
        for (int i=0; i<hidden.size(); i++){
            int numOutcoms = hidden.get(i).getOutcomes().size();
            jumps = jumps/ numOutcoms; //6
            int input = 0;
            for ( int j=0;j<NumOfOptions;j++){
                input = (j/ jumps)%numOutcoms ;
                arr_options[j][i] = input;
            }
        }

        return arr_options;
    }

    public String toString(){

        String s = this.name_XML + ":\n" ;
        for (int i=0;i<events.size();i++){
            s = s +events.get(i).toString()+ "\n";
        }
        return s;
    }
    public ArrayList<Integer> appendComponents(ArrayList<Integer> components, int[] option) {
        ArrayList<Integer> allcomponents = new ArrayList<>();
        for (int i=0 ; i<components.size();i++){
            allcomponents.add(components.get(i));
        }
        for (int i=0 ; i<option.length;i++){
            allcomponents.add(option[i]);
        }
        return allcomponents;
    }
    public ArrayList<EventNode> appendNodes(ArrayList<EventNode> query_and_evedent, ArrayList<EventNode> hidden){
        ArrayList<EventNode> appendNodes = new ArrayList<>();
        for (int i=0; i<query_and_evedent.size();i++) {
            appendNodes.add(query_and_evedent.get(i));
        }
        for (int i=0; i<hidden.size();i++){
            appendNodes.add(hidden.get(i));
        }
        return appendNodes;
    }
    public EventNode hashValue(String key){

        return hashEvents.get(key);
    }

    public int[][] optionsForF2 (ArrayList<EventNode> hidden) {
        ;
        int NumOfOptions = getNumOfOptions(hidden); //מחזיר את כמות הp() שצריך לעשות עם כל הקומבינציות של המשתנים החבואים
        int[][] arr_options = new int[NumOfOptions][hidden.size()];
        // puting data in the matrix
        int jumps = NumOfOptions;
        for (int i = 0; i < hidden.size(); i++) {
            int numOutcoms = hidden.get(i).getOutcomes().size();
            jumps = jumps / numOutcoms; //6
            int input = 0;
            for (int j = 0; j < NumOfOptions; j++) {
                input = (j / jumps) % numOutcoms;
                arr_options[j][i] = input;
            }
        }
        return arr_options;
    }
}

