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

public class BaseNet {
    ArrayList<Character> perens = new ArrayList<Character>();
    ArrayList<Character> outcomes = new ArrayList<Character>();

    public BaseNet(){}

    public void setNet(File file) throws ParserConfigurationException, IOException, SAXException {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            document.getDocumentElement().normalize();
            NodeList varList = document.getElementsByTagName("VARIABLE");
            for (int i = 0; i< varList.getLength();i++)
            {
                Node var =  varList.item(i);
                if (var.getNodeType() == Node.ELEMENT_NODE){
                    Element varElement = (Element) var;
                    String s = varElement.getElementsByTagName("NAME").item(0).getTextContent();
                    System.out.println("VARIABLE NAME: " + s);
                    for (int j=0 ; j < varElement.getElementsByTagName("OUTCOME").getLength(); j++) {
                        String s2 = varElement.getElementsByTagName("OUTCOME").item(j).getTextContent();
                        System.out.println("OUTCOME: " + s2);
                    }

                }
            }
        }


    }

