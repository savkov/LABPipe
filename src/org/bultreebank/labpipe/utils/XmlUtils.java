/*
 * LABPipe - Natural Language Processing Pipeline for Bulgarian
 * Copyright (C) 2011  Aleksandar Savkov
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bultreebank.labpipe.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.bultreebank.labpipe.data.ClarkDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Aleksandar Savkov
 */
public class XmlUtils {

    private static final Logger logger = Logger.getLogger(XmlUtils.class.getName());

    public static void print(Document doc, OutputStream out) {

        TransformerFactory tfactory = TransformerFactory.newInstance();
        Transformer serializer;
        try {
            serializer = tfactory.newTransformer();
            //Setup indenting to "pretty print"
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            serializer.transform(new DOMSource(doc), new StreamResult(out));
            if (!out.equals(System.out)) {
                out.close();
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_IO, ex);
        } catch (TransformerException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public static String printToString(Document doc) throws UnsupportedEncodingException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        print(doc, baos);
        return baos.toString(ServiceConstants.PIPE_CHARACTER_ENCODING);

    }

    public static boolean xmlDiff(String file1, String file2) {
        try {

            System.out.print("Test file: " + file1 + "\nGold file: " + file2);

            Document doc1 = ClarkDocumentBuilder.buildClarkDocument(new FileInputStream(file1));
            Document doc2 = ClarkDocumentBuilder.buildClarkDocument(new FileInputStream(file2));
            Document diff = ClarkDocumentBuilder.buildClarkDocument();

            Element diffs = diff.createElement("diffs");
            diff.appendChild(diffs);

            boolean diffBol = diffNode(doc1.getDocumentElement(), doc2.getDocumentElement(), diff);

            if (diffBol) {
                System.out.println("\t...different!\n");
            } else {
                System.out.println("\t...identical!\n");
            }

            if (diff.getElementsByTagName("diff").getLength() > 0) {
                XmlUtils.print(diff, System.out);
            }
            
            return diffBol;

        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_FILE_NOT_FOUND, ex);
        }
        
        return false;

    }

    @SuppressWarnings("LoggerStringConcat")
    private static boolean diffNode(Node motherNode1, Node motherNode2, Document log) {

        NodeList children1 = motherNode1.getChildNodes();
        NodeList children2 = motherNode2.getChildNodes();

        boolean diffBol = false;
        
        if (children1.getLength() != children2.getLength()) {
            logger.warning("Node children counts differ: " + motherNode1.getNodeName() + " " + motherNode2.getNodeName());
            diffBol = true;
        }

        Element diffs = log.getDocumentElement();
        Element diff = log.createElement("diff");

        int length = (children1.getLength() <= children2.getLength()) ? children1.getLength() : children2.getLength();

        

        for (int i = 0; i < length; i++) {

            Node n1 = children1.item(i);
            Node n2 = children2.item(i);

            if (n1 instanceof Element) {

                if (n1.getNodeName().equals("token")) {
                    //logger.log(Level.ALL, "");
                }

                boolean names = n1.getNodeName().equals(n2.getNodeName());
                boolean text = n1.getTextContent().equals(n2.getTextContent());
                boolean attributes = diffAttributes(n1, n2);
                boolean hasElements1 = containsElements(n1);
                boolean hasElements2 = containsElements(n2);

                if (hasElements1 && hasElements2) {
                    diffBol = (diffBol | diffNode(n1, n2, log)) ? true : false;
                }

                if (names && attributes && ((hasElements1 && hasElements2) || text)) {
                    continue;
                }

                diffBol = true;

                diff = log.createElement("diff");
                Node dup1 = log.importNode(n1, true);
                Node dup2 = log.importNode(n2, true);
                diff.appendChild(dup1);
                diff.appendChild(dup2);
                diffs.appendChild(diff);

            }

        }

        return diffBol;
    }

    public static boolean diffAttributes(Node n1, Node n2) {
        NamedNodeMap n1Atts = n1.getAttributes();
        NamedNodeMap n2Atts = n2.getAttributes();

        if (n1Atts.getLength() != n2Atts.getLength()) {
            return false;
        }

        for (int i = 0; i < n1Atts.getLength(); i++) {
            Node a1 = n1Atts.item(i);
            Node a2Val = n2Atts.getNamedItem(a1.getNodeName());
            if (a2Val == null || !a1.getNodeValue().equals(a2Val.getNodeValue())) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsElements(Node n) {
        NodeList children = n.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {

            if (children.item(i) instanceof Element) {
                return true;
            }

        }

        return false;
    }
}
