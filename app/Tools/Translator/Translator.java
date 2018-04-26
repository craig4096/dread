
import java.io.File;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.net.URLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.net.MalformedURLException;

import org.json.JSONObject;
import org.json.JSONException;

import org.apache.commons.lang3.StringEscapeUtils;


public class Translator
{

    private static boolean filter(String name)
    {
        if(name.equals("app_name"))
            return true;
            
        if(name.startsWith("weapon"))
            return true;
            
        return false;
    }

    private static String translateString(String enString, String toLanCode, String email)
    {
        // replace all \' with '
        enString = enString.replace("\\'", "'");
    
        if(enString.length() > 500)
        {
            System.out.println("Error: string is greater than 500 characters");
            return "Error";
        }
    
        String out = enString;
        try
        {
            String urlName =
                    "http://api.mymemory.translated.net/get?q=" +
                    URLEncoder.encode(enString, "UTF-8") +
                    "&langpair=en|" + 
                    URLEncoder.encode(toLanCode, "UTF-8") +
                    "&mt=1";
                    
            if(email.length() > 0)
            {
                urlName += ("&de=" + URLEncoder.encode(email, "UTF-8"));
            }
            URL url = new URL(urlName);
                    
            URLConnection connection = (URLConnection)url.openConnection();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    connection.getInputStream()));
                    
            StringBuffer buffer = new StringBuffer();
            String inputLine;
            while((inputLine = reader.readLine()) != null)
            {  
                buffer.append(inputLine);
            }

		    JSONObject json = new JSONObject(buffer.toString());
            JSONObject responseData = json.getJSONObject("responseData");
            
            out = StringEscapeUtils.unescapeHtml4(responseData.getString("translatedText"));
            
            // format the text for android:
            // http://developer.android.com/guide/topics/resources/string-resource.html#FormattingAndStyling:
            out = out.replace("'", "\\'");
        }
        catch(MalformedURLException e)
        {
            System.out.println("Malformed URL Exception");
        }
        catch(IOException e)
        {
            System.out.println("IO Exception: " + e.toString());
        }
        catch(JSONException e)
        {
            System.out.println("Corrupt json file returned from MyMemory");
        }
        // send request
        // wait for response
        // parse json
        // return translated string
        return out;
    }

    public static void main(String[] args)
    {
        String inputFileName = args[0];
        String outputFileName = args[1];
        String outputLanguageCode = args[2];
        String email = "";
        if(args.length > 3)
        {
            email = args[3];
        }
      
        // open output file for writing
        File outFile = new File(outputFileName);
        // open input file for reading
        File inFile = new File(inputFileName);
        try
        {
            // parse xml input
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
            
            Document inDoc = docBuilder.parse(inFile);
            inDoc.getDocumentElement().normalize();
            
            Document outDoc = docBuilder.newDocument();
            Element outRoot = outDoc.createElementNS("http://schemas.android.com/apk/res/android", "resources");
            outDoc.appendChild(outRoot);
            

            NodeList strings = inDoc.getElementsByTagName("string");
            for(int i = 0; i < strings.getLength(); ++i)
            {
                Node node = strings.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element element = (Element)node;
                    String name = element.getAttribute("name");
                    if(!filter(name))
                    {
                        System.out.println("Translating String: " + name);
                     
                        // Translate string
                        String translated = translateString(element.getTextContent(), outputLanguageCode, email);
                        
                        // write to xml
                        Element outElement = outDoc.createElement("string");
                        outElement.setAttribute("name", name);
                        outElement.setTextContent(translated);
                        outRoot.appendChild(outElement);
                    }
                }
            }
            
            
            // Any string arrays
            NodeList strArrays = inDoc.getElementsByTagName("string-array");
            for(int i = 0; i < strArrays.getLength(); ++i)
            {
                Node node = strArrays.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element arrayElem = (Element)node;
                    String name = arrayElem.getAttribute("name");
                
                    Element outArray = outDoc.createElement("string-array");
                    outArray.setAttribute("name", name);
                    System.out.println("Translating string array: " + name);
                    
                    NodeList items = node.getChildNodes();
                    for(int j = 0; j < items.getLength(); ++j)
                    {
                        Node n = items.item(j);
                        if(n.getNodeType() == Node.ELEMENT_NODE)
                        {
                            String translated = translateString(n.getTextContent(),
                                outputLanguageCode, email);
                                
                            Element outElement = outDoc.createElement("item");
                            outElement.setTextContent(translated);
                            outArray.appendChild(outElement);
                        }
                    }
                    outRoot.appendChild(outArray);
                }
            }

            // finally write the output file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(
                new DOMSource(outDoc),
                new StreamResult(outFile));
            
        }
        catch(Exception e)
        {
        }
    }

}
