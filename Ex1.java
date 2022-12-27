import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import java.io.*;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.IOException;
public class Ex1 {

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        PrintWriter out = new PrintWriter("output.txt");
        BufferedReader br
                = new BufferedReader(new FileReader("input.txt"));
        String XML_name = br.readLine();
        ArrayList<String> allQueries = new ArrayList<>(); //string arraylist for all Querys
        String query = br.readLine();
        while (query !=null) {
            allQueries.add(query);
            query = br.readLine();
        }
        BaseNet B = new BaseNet (XML_name); // baseNet new object
        for (int k=0;k<allQueries.size();k++){ // goes thru all Query's
            ArrayList<EventNode> query_and_evidence = new ArrayList<>();
            ArrayList<Integer> components =new ArrayList<>();
            String queries = allQueries.get(k);
            char numFunction = queries.charAt(queries.length()-1); // brings the number of function
            queries = queries.substring(0,queries.length()-3); // cuts the three last unnesesery notes
            queries= queries.replace("P","");
            queries = queries.replace("(","");
            queries =queries.replace(")","");
            queries =queries.replace("|",",");
            String[] queriesSplit = queries.split(",");

            for (String a : queriesSplit) { // example: A=T V1=F B=V3
                String[] pear = a.split("="); // each 'pear'(A=T V1=F B=V3) split by '='
                String name_node = pear[0];
                for (int i=0;i<B.events.size();i++) //finds the event node that matches the name
                {
                    if(B.getEvents().get(i).getName().equals(name_node))
                    {
                        query_and_evidence.add(B.getEvents().get(i)); // adds the node to an array list of event nods
                        String StringToAdd = pear[1]; // the component
                        int toAdd = B.getEvents().get(i).hashValue(StringToAdd); //gets the value of the component (T=0 F=1 V3=2..)
                        components.add(toAdd); //adds the component in an arraylist at the same index that the node is in
                        break;
                    }
                }
            }
            /**
             * for numFunction =2 -
             * The elimination order of the variables is according to the ABC order
             * for numFunction =3 -
             * The elimination of the variables is according to the total lengths of the factors in which
             * the variable is found, from the smallest to the largest.
             * In addition, when we reach a situation where all the tables in which the variable appears in
             * consist only the variable itself, we can move on to the next variable because this means
             * that the variable does not depend on the query variable.
             */

            if(numFunction=='2'|| numFunction=='3') {
                if(isInTable(query_and_evidence)){ // if the answer to the question is already in the table
                    double [] ans = new double[3];
                    ans[0] = getTA(query_and_evidence,components);
                    ans[1]=0;
                    ans[2]=0;
                    out.println(String.format("%.5f", ans[0])+","+  (int)ans[1]+ ","+  (int)ans[2]);
                }
                else {
                    if(numFunction=='2') {
                        double[] ans = B.function2(query_and_evidence, components, 2);
                        out.println(String.format("%.5f", ans[0]) + "," + (int) ans[1] + "," + (int) ans[2]);
                    }
                    else {
                        double[] ans = B.function2(query_and_evidence, components, 3);
                        out.println(String.format("%.5f", ans[0]) + "," + (int) ans[1] + "," + (int) ans[2]);
                    }

                }
            }

            if(numFunction=='1' ) {

                  if(!isInTable(query_and_evidence)) { // if the answer to the question is *not* already in the table
                    /* n
                   double mone - for the normalization this is the answer to P(A,B,C,D,E)
                   double div - is P(A,B,C,D,E)+ P(-A,B,C,D,E) + ....
                    */
                    double [] ans;
                    double [] temp;
                    int dontDo = components.get(0);
                    ans =  B.function1(query_and_evidence, components);
                    double mone = ans[0];
                    double div = 0;
                    for (int i = 0; i < query_and_evidence.get(0).getOutcomesSize(); i++)
                    {
                        if( i!= dontDo){
                            components.set(0, i);
                            temp = B.function1(query_and_evidence, components);
                            div += temp[0];
                            ans[1] += temp[1];
                            ans[2] += temp[2];
                        }
                    }

                    ans[0] = mone / (div+mone); //normalization
                    ans[1]+= query_and_evidence.get(0).getOutcomesSize()-1; //P(D1=F|C1=T,C2=v1,C3=T,A1=T),1
                    out.println(String.format("%.5f", ans[0])+","+  (int)ans[1]+ ","+  (int)ans[2]);
                }
                else // the case when the answer is in the query table we will do the same formula
                {
                    double [] ans = new double[3];
                    ans[0] = getTA(query_and_evidence,components);
                    ans[1]=0;
                    ans[2]=0;
                    out.println(String.format("%.5f", ans[0])+","+  (int)ans[1]+ ","+  (int)ans[2]);
                }
            }
        }
        out.close();
    }
/* a formula to find the cell in the table -
     the value of event1 * num of rows in the query table/number of components that event1 has +
     the value of event2 * num of rows in the query table/number of components that event2 has *number of components that event2 has +
     the value of event3 * num of rows in the query table/number of components that event1 * event2 * event3......
    */
    /**
     * in case that the answer to the query is in the cpt of the query variable
     * we can find the wanted cell with this formula that I came up with, which is written above
     * @param query_and_evidence
     * @param components
     * @return
     */

    public static double getTA(ArrayList<EventNode> query_and_evidence,ArrayList<Integer> components){
        double ans;
        int column = components.get(0);
        int row =0;
        int div =1;
        ArrayList<EventNode> ordered_query_and_evidence = new ArrayList<>();
        ordered_query_and_evidence.add(query_and_evidence.get(0));
        ArrayList<Integer> ordered_components = new ArrayList<>();
        ordered_components.add(components.get(0));
        for (int i = 0; i < query_and_evidence.get(0).getParents().size(); i++) {
            for (int j = 1; j < query_and_evidence.size(); j++) {
                if( (query_and_evidence.get(0).getParents().get(i).name.equals(query_and_evidence.get(j).getName()))){
                    ordered_query_and_evidence.add(query_and_evidence.get(j));
                    ordered_components.add(components.get(j));
                }
            }

        }
        int rowsOfQuery = query_and_evidence.get(0).getCptTable().length ;
        for (int i=1;i<query_and_evidence.size();i++)  //goes threw all the events
        {
            div*= ordered_query_and_evidence.get(i).getOutcomesSize();
            int a = ordered_components.get(i);
            row+= a*(rowsOfQuery/div);
        }
        ans = (query_and_evidence.get(0).getCptTable())[row][column]; //P(D1=T|C2=v1,C3=F),1

       return ans;
    }

    /**
     * checks if the answer to the query can be fined in tne cpt of the query variable -
     * means if all the parents of the query are thr evidence variables
     * @param query_and_evidence
     * @return
     */
    public static boolean isInTable(ArrayList<EventNode> query_and_evidence) {

        boolean isParent = false;
        int countNumOfParents = 0;
        for (int i = 1; i < query_and_evidence.size(); i++) { // finds if the query question is already in the table
            isParent = false;
            for (int j = 0; j < query_and_evidence.get(0).getParents().size(); j++) { // goes threw the query parents
                if (query_and_evidence.get(i).getName().equals(query_and_evidence.get(0).getParents().get(j).getName())) {
                    isParent = true; //
                    countNumOfParents++;
                    break;
                }
            }
            if (!isParent) {
                return false;
            }
        }
        if(countNumOfParents!=query_and_evidence.get(0).getParents().size()) { // asks if the events are all of the parents of the query
            return false;
        }
        return true;

    }

}



