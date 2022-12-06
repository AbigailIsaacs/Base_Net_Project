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
                boolean isPerent=false;
                int countNumOfPerents=0;
                for (int i=1 ; i<query_and_evedent.size();i++) { // finds if the query question is already in the table
                    isPerent=false;
                    for (int j = 0; j < query_and_evedent.get(0).getPerents().size(); j++) { // goes threw the query perents
                        if (query_and_evedent.get(i).getName().equals(query_and_evedent.get(0).getPerents().get(j).getName())) {
                            isPerent=true; //
                            countNumOfPerents++;
                            break;
                        }
                    }
                    if (!isPerent){
                        break;
                    }
                }
                if(!isPerent|| countNumOfPerents!=query_and_evedent.get(0).getPerents().size()) // asks if the query question is already in the table
                {
                    /* a formula to find the cell in the table -
                     the component of event1 * num of rows in the query table/number of components that event1 has +
                     the component of event2 * num of rows in the query table/number of components that event2 has *number of components that event2 has +
                     the component of event3 * num of rows in the query table/number of components that event1 * event2 * event3......
                    */

                    double [] ans;
                    double [] temp;
                    ans =  B.function1(query_and_evedent, components);
                    double mone = ans[0];
                    double div = mone;
                    for (int i = 1; i < query_and_evedent.get(0).getOutcomsSize(); i++)
                    {
                        components.set(0, i);
                        temp = B.function1(query_and_evedent, components);
                        div += temp[0];
                        ans[1]+=temp[1];
                        ans[2]+=temp[2];
                    }
                    System.out.println("********************");
                    ans[0] = mone / div;
                    ans[1]+= query_and_evedent.get(0).getOutcomsSize()-1;
                    System.out.println("answer =" +ans[0]+" num plus= "+  ans[1]+ "num multy"+  ans[2]);
                }
                else // the case when the answer is in the query table we will do the same formula
                {
                    int colum = components.get(0);
                    int row =0;
                    int div =1;
                    int rowsOfQuery = query_and_evedent.get(0).getCptTable().length ;
                    for (int i=1;i<query_and_evedent.size();i++)  //goes threw all the events
                    {
                        div*= query_and_evedent.get(i).getOutcomsSize();
                        int a = components.get(i);
                        row+= a*(rowsOfQuery/div);
                    }
                    double ans = (query_and_evedent.get(0).getCptTable())[row][colum]; //P(D1=T|C2=v1,C3=F),1
                    System.out.println("##################");
                    System.out.println(ans);
                }
            }
        }
    }
}



