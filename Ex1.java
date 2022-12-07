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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.io.File;
import java.util.Scanner;

public class Ex1 {

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter xml name");
        String XML_name = scanner.nextLine();
        System.out.println("enter Querys");

        ArrayList<String> allQuerys = new ArrayList<>(); //string arraylist for all Querys
        String query = scanner.nextLine();
       // while (query!=endof file) {
            allQuerys.add(query);
            query = scanner.nextLine();
       // }

        BaseNet B = new BaseNet (XML_name); // baseNet new object

        ArrayList<EventNode> query_and_evedent = new ArrayList<>();
        ArrayList<Integer> components =new ArrayList<>();
        for (int k=0;k<allQuerys.size();k++){ // goes thru all Query's
            String querys = allQuerys.get(k);
            char numFanction = querys.charAt(querys.length()-1); // brings the number of function
            querys = querys.substring(0,querys.length()-3); // cuts the three last unnesesery notes
            querys= querys.replace("P","");
            querys = querys.replace("(","");
            querys =querys.replace(")","");
            querys =querys.replace("|",",");
            String[] querysSplit = querys.split(",");

            for (String a : querysSplit) { // A=T V1=F B=V3
                String[] pear = a.split("="); // each 'pear'(A=T V1=F B=V3) split by '='
                String name_node = pear[0];
                for (int i=0;i<B.events.size();i++) //finds the event node that maches the name
                {
                    if(B.getEvents().get(i).getName().equals(name_node))
                    {
                        query_and_evedent.add(B.getEvents().get(i)); // adds the node to an array list of event nods
                        String StringtoAdd = pear[1]; // the component
                        int toAdd = B.getEvents().get(i).hashValue(StringtoAdd); //gets the value of the component (T=0 F=1 V3=2..)
                        components.add(toAdd); //adds the component in an arraylist at the same index that the node is in
                        break;
                    }
                }
            }
            if(numFanction=='1' )
            {
                if(!isInTable(query_and_evedent)) { // if the answer to the question is *not* already in the table
                    /* n
                   double mone - for the normalization this is the answer to P(A,B,C,D,E)
                   double div - is P(A,B,C,D,E)+ P(-A,B,C,D,E) + ....
                    */
                    double [] ans;
                    double [] temp;
                    int dontDo = components.get(0);
                    ans =  B.function1(query_and_evedent, components);//P(D1=F|C1=T,C2=v1,C3=T,A1=T),1
                    double mone = ans[0];
                    double div = 0;
                    for (int i = 0; i < query_and_evedent.get(0).getOutcomesSize(); i++)
                    {
                        if( i!= dontDo){
                            components.set(0, i);
                            temp = B.function1(query_and_evedent, components);
                            div += temp[0];
                            ans[1] += temp[1];
                            ans[2] += temp[2];
                        }
                    }
                    System.out.println("********************"); //P(B0=v3|C3=T,B2=F,C2=v3),1
                    ans[0] = mone / (div+mone); //normalization
                    ans[1]+= query_and_evedent.get(0).getOutcomesSize()-1; //P(D1=F|C1=T,C2=v1,C3=T,A1=T),1
                    System.out.println("answer =" +ans[0]+" num plus= "+  ans[1]+ "num multy"+  ans[2]);
                }
                else // the case when the answer is in the query table we will do the same formula
                {
                    double [] ans = new double[3];
                    ans[0] = getTA(query_and_evedent,components);
                    ans[1]=0;
                    ans[2]=0;
                    System.out.println("##################");
                    System.out.println("answer =" +ans[0]+" num plus= "+  ans[1]+ "num multy"+  ans[2]);
                }
            }
        }
    }

    public static double getTA(ArrayList<EventNode> query_and_evedent,ArrayList<Integer> components){
        double ans;
        int column = components.get(0);
        int row =0;
        int div =1;
        int rowsOfQuery = query_and_evedent.get(0).getCptTable().length ;
        for (int i=1;i<query_and_evedent.size();i++)  //goes threw all the events
        {
            div*= query_and_evedent.get(i).getOutcomesSize();
            int a = components.get(i);
            row+= a*(rowsOfQuery/div);
        }
        ans = (query_and_evedent.get(0).getCptTable())[row][column]; //P(D1=T|C2=v1,C3=F),1

       return ans;
    }
    public static boolean isInTable(ArrayList<EventNode> query_and_evedent) {

        boolean isParent = false;
        int countNumOfParents = 0;
        for (int i = 1; i < query_and_evedent.size(); i++) { // finds if the query question is already in the table
            isParent = false;
            for (int j = 0; j < query_and_evedent.get(0).getParents().size(); j++) { // goes threw the query parents
                if (query_and_evedent.get(i).getName().equals(query_and_evedent.get(0).getParents().get(j).getName())) {
                    isParent = true; //
                    countNumOfParents++;
                    break;
                }
            }
            if (!isParent) {
                return false;
            }
        }
        if(countNumOfParents!=query_and_evedent.get(0).getParents().size()) { // asks if the events are all of the parents of the query
            return false;
        }
        return true;
    }
}



