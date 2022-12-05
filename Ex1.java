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

        Scanner scanner = new Scanner(System.in);  // Create a Scanner object
        System.out.println("Enter xml name");
        String XML_name = scanner.nextLine();
        System.out.println("enter Querys");
        String querys = scanner.nextLine();
        BaseNet B = new BaseNet (XML_name);
        ArrayList<EventNode> query_and_evedent = new ArrayList<>();
        ArrayList<Integer> components =new ArrayList<>();
        char numFanction = querys.charAt(querys.length()-1);

        querys = querys.substring(0,querys.length()-3);
/// הוספה של משתנה הקווארי לאריי ליסט
        String name_query = querys.substring(2,3);

        for(int i=0; i<B.getEvents().size();i++) {
            if(B.getEvents().get(i).getName().equals(name_query)){
                query_and_evedent.add(B.getEvents().get(i));
                String StringtoAdd = querys.substring(2+2,3+2);
                int toAdd = B.getEvents().get(i).hashValue(StringtoAdd);
                components.add(toAdd);
                break;
            }
        }
        /// הוספה של משתני האווידנס לאריי ליסט
        for(int i =6; i<querys.length()-1;i=i+4) {   //P(B=T|J=T,M=T),1
            String name_evidence = querys.substring(i,i+1);
            for(int j=0; j<B.getEvents().size();j++) {
                if (B.getEvents().get(j).getName().equals(name_evidence)) {
                    query_and_evedent.add(B.getEvents().get(j));
                    String stringtoAdd = querys.substring(i+2,i+3);
                    System.out.println(stringtoAdd);
                    int toAdd = B.getEvents().get(j).hashValue(stringtoAdd);
                    components.add(toAdd);
                    break;
                }
            }
        }
        System.out.println("query and evidence: "+query_and_evedent + "numFanction: "+numFanction);
        if(numFanction=='1' ){
            double div =0;
            double mone = B.function1(query_and_evedent,components);
            System.out.println("mone"+ mone);
            for(int i=0;i<query_and_evedent.get(0).getOutcomsSize();i++){
                components.set(0,i);
               div+= B.function1(query_and_evedent,components);
                System.out.println("q"+i+" "+ div);
            }
            System.out.println("********************");
            System.out.println(mone/div);
        }

        }
}



