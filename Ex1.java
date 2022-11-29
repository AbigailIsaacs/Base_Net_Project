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

public class Ex1 {

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {

        File file = new File("alarmNet.xml");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        document.getDocumentElement().normalize();

        ArrayList<EventNode> events = new ArrayList<>();// מערך של האובייקטים

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
                for (int j = 0; j < varElement.getElementsByTagName("OUTCOME").getLength(); j++) {
                    String s2 = varElement.getElementsByTagName("OUTCOME").item(j).getTextContent();

                    e.getOutcoms(s2); // מוסיפה לנואוד את האוטוקאם
                }
            }
        }


        NodeList defList = document.getElementsByTagName("DEFINITION");

        for (int i = 0; i < defList.getLength(); i++) // מעבר על כל הטבלאות
        {
            Node def = defList.item(i);

            if (def.getNodeType() == Node.ELEMENT_NODE) {
                Element defElement = (Element) def;
                for (int j = 0; j < defElement.getElementsByTagName("GIVEN").getLength(); j++) {
                    String s2 = defElement.getElementsByTagName("GIVEN").item(j).getTextContent();
                    events.get(i).getPerents().add(s2); // מוסיפה הורים
                }
                String s2 = defElement.getElementsByTagName("TABLE").item(0).getTextContent();
                events.get(i).getCPT().add(s2); // מוסיפה הורים
            }
        }
        System.out.println(events);
    }
}


