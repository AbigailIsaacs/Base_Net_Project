import java.util.ArrayList;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;

public class BaseNet {
    ArrayList<EventNode> events ;// מערך של כל המאורעות
    String name_XML ;

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
                    e.GetOutcoms().put(outcome, j);//  מוסיפה לנואוד את האוטוקאם

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
                    for (int k = 0; k < events.size(); k++) {
                        if (events.get(k).getName().equals(name)) {
                            events.get(i).getPerents().add(events.get(k)); // מוסיפה הורים
                        }
                    }
                }
                String numbers = defElement.getElementsByTagName("TABLE").item(0).getTextContent();
                StringTokenizer cptNumbers = new StringTokenizer(numbers);
                while (cptNumbers.hasMoreTokens()) {
                    double num = Double.parseDouble(cptNumbers.nextToken());
                    events.get(i).getCPT().add(num);// מוסיפה את המספרים
                }
            }
        }
        for (int i=0 ; i<events.size();i++){
            events.get(i).createCPT();
        }
        return events;

    }
    //****************** calculateQustion
    public double [] function1 (ArrayList<EventNode> query_and_evedent, ArrayList<Integer> components) {
        double ans=0;
        int numOfPlus =0;
        double [] toReturn = new double [3];
        double [] fronCalc;
        int numMultiplcations =0;
        ArrayList<EventNode> allNodes = appendNodes(query_and_evedent,getHidden(query_and_evedent));
        int[][] options = options(query_and_evedent); // מחזיר מערך עם כל האופציות

        for(int i=0;i<options.length;i++) {//מעבר על כל השורות
            int [] arr = new int[options[0].length];
            for (int j=0;j<options[0].length;j++){
                arr[j] = options[i][j];
            }
            ArrayList<Integer> allComponents = appendComponents(components,arr);

            fronCalc =calc(allNodes, allComponents); // שולח לפונקציה חישוב
            ans+= fronCalc[0];
            numOfPlus++;
            numMultiplcations += fronCalc[1];
        }
        toReturn[0] = ans;
        toReturn[1] = numOfPlus-1;
        toReturn [2] = numMultiplcations;
        return toReturn;
    }
    public double [] calc (ArrayList<EventNode> allNodes,ArrayList<Integer> allComponents) {
        double [] toReturn = new double [2];
        double ans =1;
        int numMultiplcations =0;
        for(int i=0; i<allNodes.size();i++){ // מעבר על כל מאורע
            int colum = allComponents.get(i);
            int row =0 ;
            int divide =1;
            for (int j=0 ; j<allNodes.get(i).getPerents().size();j++){ //   מעבר על כל ההורים של המאורע
                EventNode perent = allNodes.get(i).getPerents().get(j);
                int petentComponent =0;
                for(int k=0;k<allNodes.size();k++){ // to find the index of the perent
                    if(perent.getName().equals(allNodes.get(k).getName())){
                        petentComponent = allComponents.get(k);
                    }
                }
                divide= divide*(perent.getOutcomsSize());
                int rowsOfQuery = allNodes.get(i).createCPT().length ;
                row = row + (petentComponent*(rowsOfQuery/divide)) ;
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
    public int[][] options (ArrayList<EventNode> query_and_evedent){
        ArrayList<EventNode> hidden = getHidden(query_and_evedent) ;
        int NumOfOptions = getNumOfOptions(hidden); //מחזיר את כמות הp() שצריך לעשות עם כל הקומבינציות של המשתנים החבואים
        int[][] arr_options = new int[NumOfOptions][hidden.size()];
        // puting data in the matrix
        int jumps = NumOfOptions;
        for (int i=0; i<hidden.size(); i++){
            int numOutcoms = hidden.get(i).getOutcoms().size();
            jumps = jumps/ numOutcoms; //6
            int input = 0;
            for ( int j=0;j<NumOfOptions;j++){
                input = (j/ jumps)%numOutcoms ;
                arr_options[j][i] = input;
            }
        }

        return arr_options;
        }
    private int getNumOfOptions(ArrayList<EventNode> hidden) {
        int sum =1;
        for (int i=0; i<hidden.size();i++){
            sum= sum*(hidden.get(i).getOutcomsSize());
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

}

