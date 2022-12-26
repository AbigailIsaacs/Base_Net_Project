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
 * hashEvents - a hashmap from the mane of a node to his EventNode object
 */
public class BaseNet {
    ArrayList<EventNode> events ;
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

    /**
     * get all events
     * @return
     */
    public ArrayList<EventNode> getEvents(){

        return events;
    }

    /**
     *
     * @param key
     * @return the EventNode by his key value (String name of the event)
     */
    public EventNode hashValue(String key){
        return hashEvents.get(key);
    }

    /**
     * function1 - Calculate the probability in the way described on slide 67 in the course presentation
     * @param query_and_evidence
     * @param components
     * @return an array size 3 :
     * toReturn[0] = the probability of the question
     * toReturn[1] = the number of connection operations.
     * toReturn[2] = the number of multiplication operations.
     */
    public double [] function1 (ArrayList<EventNode> query_and_evidence, ArrayList<Integer> components) {
        double ans=0;
        int numOfPlus =0;
        double [] toReturn = new double [3];
        double [] fromCalc;
        int numMultiplcations =0;
        ArrayList<EventNode> allNodes = appendNodes(query_and_evidence,getHidden(query_and_evidence));
        int[][] options = options(query_and_evidence); // returns a matrix where each row is a combination of the hidden variables

        for(int i=0;i<options.length;i++) {//goes through all rows
            int [] arr = new int[options[0].length];
            for (int j=0;j<options[0].length;j++){ // copies the row
                arr[j] = options[i][j];
            }
            ArrayList<Integer> allComponents = appendComponents(components,arr);

            fromCalc =calc(allNodes, allComponents); // returns the answer to the query with the hidden variables value is in arr
            ans+= fromCalc[0];
            numOfPlus++;
            numMultiplcations += fromCalc[1];
        }
        toReturn[0] = ans;
        toReturn[1] += numOfPlus-1;
        toReturn [2] += numMultiplcations;
        return toReturn;
    }

    /**
     * function2
     * @param query_and_evidence
     * @param components
     * @param fanction
     * @return an array size 3 :
     * toReturn[0] = the probability of the question
     * toReturn[1] = the number of connection operations.
     * toReturn[2] = the number of multiplication operations.
     */
    public double [] function2 (ArrayList<EventNode> query_and_evidence, ArrayList<Integer> components,int fanction){
        double [] toReturn = new double [3];
        ArrayList<EventNode> hiddenAncestor = getAncestor(query_and_evidence);
        ArrayList<Factor> factors=  createAllFactors(hiddenAncestor);
        for (int i = 0; i < query_and_evidence.size(); i++) { // deleting query_and_evidence from hiddenAncestor
            hiddenAncestor.remove(0);
        }
        // because we know the desired outcome of the Evidences we want to eliminate all the other outcomes
        eliminateAllEvidence(query_and_evidence,components,factors);
        if(fanction==2) {
            // sorting the factors by there name
            hiddenAncestor.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
        }
        if(fanction==3){
            hiddenAncestor.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
            hiddenAncestor = appearsTheLeast(factors,hiddenAncestor);
        }

        for (int i=0; i<hiddenAncestor.size();i++){
            //creating an Array list of the factors that contains the event hiddenAncestor.get(i)
            ArrayList<Factor> factorEvent = cerateArrFactorsForHidden(hiddenAncestor.get(i),factors);
            Collections.sort(factorEvent, Comparator.comparingInt(p -> p.getFactor().length));
            if(fanction==3){
                // in case that all the factors in factorEvent are consists only the Hidden event,
                // then we know that it is not depending on the query or evidence
                // therefore we don't need to calculate him.
                boolean b = true;
                for (int j = 0; j < factorEvent.size(); j++) {
                       if( factorEvent.get(j).factor_name.size()!=1){
                           b = false;
                           break;
                       }
                }
                if (b)
                    continue;
            }
            while (factorEvent.size()>=2){
                Factor joined= join(factorEvent.get(0),factorEvent.get(1),toReturn);// joins two factors
                factorEvent.remove(0);
                factorEvent.remove(0);
                if(joined.factor.length!=1) { // if the new factor is only one line we can delete it
                    factorEvent.add(joined);
                }
                Collections.sort(factorEvent, Comparator.comparingInt(p -> p.getFactor().length));
            }

            // eliminating the Hidden node
            Factor newF  = eliminatHidden(factorEvent.get(0),(hiddenAncestor.get(i)),toReturn);
            if(newF.factor.length!=1) { // if the new factor is only one line we can delete it
                factors.add(newF);

            }
       }
        // now we are left only with the factors of the query
        Collections.sort(factors, Comparator.comparingInt(p -> p.getFactor().length));
        while (factors.size()>=2){
            Factor joined= join(factors.get(0),factors.get(1),toReturn);
            factors.remove(0);
            factors.remove(0);
            factors.add(joined);
            Collections.sort(factors, Comparator.comparingInt(p -> p.getFactor().length));
        }
        // now we are left with one factor
        double sum = 0;
        double correctComponent =0;
        toReturn[1]--;
        for (int i = 0; i < query_and_evidence.get(0).getOutcomesSize(); i++) {
            if (i==components.get(0)){
                correctComponent = factors.get(0).factor[i];
                sum=sum+correctComponent;
                toReturn[1] += 1;
            }
            else {
                sum=sum+factors.get(0).factor[i];
                toReturn[1] += 1;
            }
        }
        toReturn[0] = correctComponent/sum;
        return toReturn;
    }

    /**
     * @param factors all factors in the net
     * @param hiddenAncestor contains the EventNodes : query, events, and evert Ancestor of them
     * @return the array list of the hiddenAncestor sorted by the number of times the event apears in the factors.
     */
    private ArrayList<EventNode> appearsTheLeast(ArrayList<Factor> factors, ArrayList<EventNode> hiddenAncestor) {

            for (int j = 0; j < hiddenAncestor.size(); j++) {
                int countAppear =0;
                for (int i = 0; i < factors.size(); i++) {
                    for (int k = 0; k < factors.get(i).factor_name.size(); k++) {
                        if (factors.get(i).factor_name.get(k).equals(hiddenAncestor.get(j).getName())) {
                            countAppear = countAppear+ factors.get(i).factor.length;
                        }
                    }
                }
                hiddenAncestor.get(j).num_apper_in_factor=countAppear;
            }
            hiddenAncestor.sort((p1, p2) -> Integer.compare(p1.num_apper_in_factor,p2.num_apper_in_factor));


        return hiddenAncestor;
    }

    /**
     * eliminating the Hidden node from the last factor that he appeared in
     * @param factor
     * @param hidden
     * @param toReturn
     * @return
     */
    public Factor eliminatHidden(Factor factor, EventNode hidden, double[] toReturn) {
        int len = factor.factor.length/hidden.getOutcomesSize();
        double [] newFactor = new double[len];
        ArrayList<String> newFactorName = new ArrayList<>();

        for (int i = 0; i < factor.factor_name.size(); i++) { // renaming the new factor
            if(!factor.factor_name.get(i).equals(hidden.getName())){
                newFactorName.add(factor.factor_name.get(i));
            }
        }
        Factor newF = new Factor (newFactorName,newFactor,this);
        if (len==1){
            return newF;
        }
        ArrayList<EventNode> Nodes = getFactorNodes(factor);
        int[][] options = optionsForF2(Nodes); // returns the number of combinations that can be done from the hidden nodes
        int jumps = factor.factor.length;
        for (int i = 0; i < factor.factor_name.size(); i++) {
            if(factor.factor_name.get(i).equals(hidden.getName())){
                jumps=jumps/hashValue(factor.factor_name.get(i)).getOutcomesSize();
                break;
            }
            jumps=jumps/hashValue(factor.factor_name.get(i)).getOutcomesSize();
        
        }
        int index=-1;

        for (int b = 0; b < factor.factor.length; b=b+(hidden.getOutcomesSize()*jumps)) {
            for (int i = 0; i <jumps ; i++) {
                toReturn[1]--;
                index++;
                for (int j = 0; j < hidden.getOutcomesSize(); j++) {
                    newFactor[index] += getTA(factor,options[b+i+(j*jumps)]);
                    toReturn[1]++;
                }
            }
        }
        return newF;
    }

    /**
     * @param factor
     * @param components
     * @return the value in the cell we reached from the values in "components"
     */
    /* a formula to find the cell in the table -
     the value of event1 * num of rows in the query table/number of components that event1 has +
     the value of event2 * num of rows in the query table/number of components that event2 has *number of components that event2 has +
     the value of event3 * num of rows in the query table/number of components that event1 * event2 * event3......
    */
    public double getTA(Factor factor,int [] components){
        int ans =0;
        double mone = factor.factor.length;
        double div =1;
        // using the formula I came up with from above
        for (int i = 0; i < components.length; i++) {
            div =div * (hashValue(factor.factor_name.get(i)).getOutcomes().size());
            ans += components[i]*mone/div;
        }
        return factor.factor[ans];
    }
    /**
     * returns an array list with all the nodes in the  factor
     * @param factor
     * @return
     */
    private ArrayList<EventNode> getFactorNodes(Factor factor) {
        ArrayList<EventNode> NodesNoHidden = new ArrayList<>();
        for (int i = 0; i < factor.factor_name.size(); i++) {
            NodesNoHidden.add(hashValue(factor.factor_name.get(i)));
        }
        return NodesNoHidden;
    }

    /**
     * @param query_and_evidence
     * @return an ArrayList that contains all the EventNodes that are query, event and there ancestors
     */
    private ArrayList<EventNode> getAncestor(ArrayList<EventNode> query_and_evidence) {
        ArrayList<EventNode> queue = new ArrayList<>(query_and_evidence);

        for (int i = 0; i < queue.size(); i++) {
            int numOfParents = queue.get(i).getParents().size();
            for (int j = 0; j < numOfParents; j++) {
                if (!isInQueue(queue.get(i).getParents().get(j).getName(),queue)){
                    queue.add(queue.get(i).getParents().get(j));
                }
            }
        }

        return queue;
    }

    private boolean isInQueue(String name, ArrayList<EventNode> queue) {
        for (int i = 0; i < queue.size(); i++) {
            if(queue.get(i).getName().equals(name)){
                return true;
            }
        }
        return false;
    }

    /**
     * joins two factors in to one which his the nodes are a union of the both nomads in the two factors.
     * @param factor1
     * @param factor2
     * @param toReturn
     * @return
     */
    public Factor join (Factor factor1,Factor factor2,double[] toReturn) {
        ArrayList<EventNode> allNodesForJoin = new ArrayList<>();
        ArrayList<String> factor_name = new ArrayList<>();

        for (int i = 0; i < factor1.factor_name.size(); i++) {
            allNodesForJoin.add(hashValue(factor1.factor_name.get(i)));
            factor_name.add(hashValue(factor1.factor_name.get(i)).name);
        }

        boolean b ;
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

        int[][] options = optionsForF2(allNodesForJoin); // returns the number of combinations that can be done from the hidden nodes

        ArrayList<Integer> F1Index = new ArrayList<>();
        //The indexes that the nods of factor 1 appears in out of all the nods of the new factor
        ArrayList<Integer> F2Index = new ArrayList<>();
        //The indexes that the nods of factor 2 appears in out of all the nods of the new factor

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
                    if (j == F1Index.get(k)) { // if the index belongs to F1
                        componentsF1[k] = options[i][j];
                    }
                }
                for (int k = 0; k < F2Index.size(); k++) {
                    if (j == F2Index.get(k)) { // if the index belongs to F2
                        componentsF2[k] = options[i][j];
                    }
                }
            }
            NewFactor[i] = (getTA(factor1,componentsF1,nodesF1)) * (getTA(factor2,componentsF2,nodesF2));
            toReturn[2]++;
        }
        Factor joined = new Factor(factor_name,NewFactor,this);


        return joined;
    }

    /**
     *
     * @param factor the factor to get a certain cell from.
     * @param components witch cell to take
     * @param nodesF all the Nodes that appear in the factor
     * @return the value of a cell in the factor.
     */
    /* a formula to find the cell in the table -
     the value of event1 * num of rows in the query table/number of components that event1 has +
     the value of event2 * num of rows in the query table/number of components that event2 has *number of components that event2 has +
     the value of event3 * num of rows in the query table/number of components that event1 * event2 * event3......
    */
    public double getTA(Factor factor,int[] components,ArrayList<EventNode> nodesF){
        int ans =0;
        double mone = factor.factor.length;
        double div =1;
        int [] newComponents = new int[components.length];
        //the problem:the order of the components is in the order of the new factor
        // so if we want to get to a certain TA we need to put the components in an order that matches the old factor.
        for (int i = 0; i <factor.factor_name.size(); i++) {
            for (int j = 0; j < nodesF.size(); j++) {
                if(factor.factor_name.get(i).equals(nodesF.get(j).getName())){
                    newComponents[i] = components[j];
                }
            }
        }
        // using a formula that I came up with above
        for (int i = 0; i < nodesF.size(); i++) {
            div =div * (hashValue(factor.factor_name.get(i)).getOutcomes().size());
            ans += newComponents[i]*mone/div;
        }
        return factor.factor[ans];
    }

    /**
     * creates an array of all factors that the hidden appears in
     * @param hidden - a hidden node
     * @param factors
     * @return
     */
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

    /**
     * because we know the desired outcome of the Evidences this function eliminates all the other outcomes
     * from all the factors
     * @param query_and_evidence
     * @param components
     * @param factors
     */
    public void eliminateAllEvidence(ArrayList<EventNode> query_and_evidence, ArrayList<Integer> components,ArrayList<Factor>factors){
        for(int i=1; i<query_and_evidence.size();i++){ //goes through all evidents
            for (int j=0;j<factors.size();j++){ //goes through all factors
                for (int k=0;k<factors.get(j).factor_name.size();k++){ ////goes through all nods in factor
                    if (factors.get(j).factor_name.get(k).equals(query_and_evidence.get(i).name)){
                        double[] newFactor = factors.get(j).eliminateEvidence(query_and_evidence.get(i),components.get(i));
                        if (newFactor.length==1){
                            factors.remove(j--);
                            break;
                        }
                        else{
                            deleteEventName(factors.get(j),query_and_evidence.get(i).name);
                            factors.get(j).factor = newFactor;
                            break;
                        }
                    }
                }
            }
        }
    }
    public void deleteEventName(Factor factor,String event){
        for (int i=0;i<factor.factor_name.size();i++){
            if (event.equals(factor.factor_name.get(i))){
                factor.factor_name.remove(i);
            }
        }
    }

    /**
     * creates a factor for each EventNode
     * @param nodes
     * @return ArrayList of all Factors in the net
     *
     *
     */
    public ArrayList<Factor> createAllFactors(ArrayList<EventNode> nodes){
        ArrayList<Factor> factors = new ArrayList();
        for (int i=0;i<nodes.size();i++) {
            factors.add(new Factor(nodes.get(i), this));
        }
        return factors;
    }


    /**
     * @param allNodes - all nods in the Bayesian network
     * @param allComponents - the components(values) that we are checking
     * @return arr[0] is the answer to the question P(A,B,C,-D-E)
     *         arr[1] number of multiplications
     */

    public double [] calc (ArrayList<EventNode> allNodes,ArrayList<Integer> allComponents) {
        double [] toReturn = new double [2];
        double ans =1;
        int numMultiplications  =0;
        for(int i=0; i<allNodes.size();i++){ // goes through every event node
            int colum = allComponents.get(i);
            int row =0 ;
            int divide =1;
            for (int j=0 ; j<allNodes.get(i).getParents().size();j++){ // goes through all parents
                EventNode parent = allNodes.get(i).getParents().get(j);
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
            numMultiplications++;
        }
        toReturn[0] = ans;
        toReturn[1]+= numMultiplications-1 ;
        return toReturn;
    }

    /**
     * returns the number of combinations that can be done from the hidden nodes
     * @param hidden
     * @return
     */
    private int getNumOfOptions(ArrayList<EventNode> hidden) {
        int sum =1;
        for (int i=0; i<hidden.size();i++){
            sum= sum*(hidden.get(i).getOutcomesSize());
        }
        return sum;
    }

    /**
     * @param query_and_evidence
     * @return an array list of all hidden events in this query
     */
    public ArrayList<EventNode> getHidden(ArrayList<EventNode> query_and_evidence){
        ArrayList<EventNode> hidden = new ArrayList<>();
        boolean b = true;
        for(int i =0; i< this.events.size(); i++){
            for (int j=0 ; j<query_and_evidence.size();j++) {
                if (events.get(i).getName().equals(query_and_evidence.get(j).getName())) {
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

    /**
     *  returns a matrix where each row is a combination of the hidden variables
     * @param query_and_evidence
     * @return
     */
    public int[][] options (ArrayList<EventNode> query_and_evidence){
        ArrayList<EventNode> hidden = getHidden(query_and_evidence) ;
        // returns the number of combinations that can be done from the hidden nodes
        int NumOfOptions = getNumOfOptions(hidden);
        int[][] arr_options = new int[NumOfOptions][hidden.size()];
        // putting data in the matrix
        int jumps = NumOfOptions;
        for (int i=0; i<hidden.size(); i++){
            int numOutcoms = hidden.get(i).getOutcomes().size();
            jumps = jumps/ numOutcoms;
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

    /**
     * appends the values of the query, event and hidden nodes in to a single array list
     * @param components
     * @param option
     * @return
     */
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

    /**
     * appends the EventNodes of the query, event and hidden nodes in to a single array list
     * @param query_and_evidence
     * @param hidden
     * @return
     */
    public ArrayList<EventNode> appendNodes(ArrayList<EventNode> query_and_evidence, ArrayList<EventNode> hidden){
        ArrayList<EventNode> appendNodes = new ArrayList<>();
        for (int i=0; i<query_and_evidence.size();i++) {
            appendNodes.add(query_and_evidence.get(i));
        }
        for (int i=0; i<hidden.size();i++){
            appendNodes.add(hidden.get(i));
        }
        return appendNodes;
    }

    /**
     * @param hidden
     * @return a matrix in which each row represents the components of each node in a certain factor,
     * all rows together are all the combinations created from the hidden variables
     */
    public int[][] optionsForF2 (ArrayList<EventNode> hidden) {
        int NumOfOptions = getNumOfOptions(hidden);
        int[][] arr_options = new int[NumOfOptions][hidden.size()];
        // putting data in the matrix
        int jumps = NumOfOptions;
        for (int i = 0; i < hidden.size(); i++) {
            int numOutcoms = hidden.get(i).getOutcomes().size();
            jumps = jumps / numOutcoms;
            int input = 0;
            for (int j = 0; j < NumOfOptions; j++) {
                input = (j / jumps) % numOutcoms;
                arr_options[j][i] = input;
            }
        }
        return arr_options;
    }

    /**
     * reads the XML file -
     * initialize the EventNodes with their name, parents and cpt table
     * initialize in this BaseNet the events and the hashEvents
     * @param nameXML
     * @return
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public ArrayList<EventNode> readXML(String nameXML) throws ParserConfigurationException, IOException, SAXException {
        File file = new File(nameXML);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        document.getDocumentElement().normalize();
        ArrayList<EventNode> events = new ArrayList<>();
        NodeList varList = document.getElementsByTagName("VARIABLE");
        for (int i = 0; i < varList.getLength(); i++) // goes throw all the events
        {
            Node var = varList.item(i);
            if (var.getNodeType() == Node.ELEMENT_NODE) {
                Element varElement = (Element) var;
                String s = varElement.getElementsByTagName("NAME").item(0).getTextContent();
                EventNode e = new EventNode(); // creates an EventNode
                e.setName(s); //
                events.add(e);
                for (int j = 0; j < varElement.getElementsByTagName("OUTCOME").getLength(); j++) {
                    String outcome = varElement.getElementsByTagName("OUTCOME").item(j).getTextContent();
                    e.getOutcomes().put(outcome, j);
                }
            }
        }

        NodeList defList = document.getElementsByTagName("DEFINITION");
        for (int i = 0; i < defList.getLength(); i++)
        {
            Node def = defList.item(i);
            if (def.getNodeType() == Node.ELEMENT_NODE) {
                Element defElement = (Element) def;
                for (int j = 0; j < defElement.getElementsByTagName("GIVEN").getLength(); j++) {
                    String name = defElement.getElementsByTagName("GIVEN").item(j).getTextContent();
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
            events.get(i).createCPT(); // creates a cpt for all events
        }
        for (int i=0;i<events.size();i++){
            hashEvents.put(events.get(i).name, events.get(i)); // creates a hash value for all events
        }
        return events;
    }
}

