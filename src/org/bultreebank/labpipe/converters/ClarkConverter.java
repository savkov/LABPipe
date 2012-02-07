/*
 * LABPipe - Natural Language Processing Pipeline for Bulgarian
 * Copyright (C) 2011 Institute for Information and Communication Technologies  
 * 
 * The development of this program was funded by the EuroMatrixPlus Project as 
 * part of the Seventh Framework Program of the European Commission.
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

package org.bultreebank.labpipe.converters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bultreebank.labpipe.data.ClarkDocumentBuilder;
import org.bultreebank.labpipe.data.Conll;
import org.bultreebank.labpipe.utils.Misc;
import org.bultreebank.labpipe.utils.ServiceConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * <code>ClarkConverter</code> provides conversion methods for CLaRK documents.
 * This class only contains methods converting to CoNLL and Line data formats. 
 * The rest of the data formats should be covered in other classes belonging to 
 * the respective formats.
 *
 * @author Aleksandar Savkov
 */
public class ClarkConverter {
    
    private static final Logger logger = Logger.getLogger(ClarkConverter.class.getName());

    /**
     * Reads in a CLaRK document from the <code>InputStream</code> into, converts
     * it to {@link Conll} object and writes its <code>String</code> 
     * representation into the <code>OutputStream</code>.
     * 
     * @param   is  <code>InputStream</code> reading a String representation 
     *              of a CLaRK document.
     * @param   os  <OutputStream</code> writing the resulting <code>CoNLL</code>
     *              document.
     * @param   conllMap    <code>Map</code> containing connections between full
     *                      BTB tags and their respective CoNLL forms.
     */
    public static void toConll(InputStream is, OutputStream os, Properties conllMap) {
        
        try {
            os.write(toConll(ClarkDocumentBuilder.buildClarkDocument(is), conllMap).toString().getBytes(ServiceConstants.PIPE_CHARACTER_ENCODING));
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_UNSUPPORTED_ENCODING, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_IO, ex);
        }
        
    }

    /**
     * Converts a CLaRK document to a CoNLL object.
     * 
     * @param   doc CLaRK document
     * @param   conllMap    <code>Map</code> containing connections between full
     *                      BTB tags and their respective CoNLL forms.
     * @return {@link Conll}
     * 
     */
    public static Conll toConll(Document doc, Properties conllMap) {
        
        Conll conll = new Conll();
        ArrayList<String> sentence;
        ArrayList<String> line;

        NodeList sentences = doc.getElementsByTagName("s");

        for (int i = 0; i < sentences.getLength(); i++) {
            NodeList tokens = ((Element) sentences.item(i)).getElementsByTagName("tok");
            sentence = new ArrayList(100);
            conll.add(sentence);

            for (int j = 0; j < tokens.getLength(); j++) {
                line = new ArrayList(10);

                // Conll ID
                line.add(String.valueOf(j+1));

                Element token = (Element) tokens.item(j);

                // Conll Token
                line.add(token.getTextContent().replaceAll(" ", "_"));

                //Conll Lemma
                if (token.hasAttribute("lm")) {
                    line.add(token.getAttribute("lm").replaceAll(" ", "_"));
                } else {
                    line.add("_");
                }

                String tag;
                if (token.hasAttribute("ana")) {
                    tag = token.getAttribute("ana");
                } else if (token.hasAttribute("svm")) {
                    tag = token.getAttribute("svm");
                } else {
                    continue;
                }

                // Short tag (BTB first letter)
                if (tag.contains("punct")) {
                    line.add("Punct");
                } else {
                    line.add(String.valueOf(tag.charAt(0)));
                }

                // Long tag
                if (tag.contains("punct") || tag.contains("Punct")) {
                    line.add("Punct");
                } else if (tag.length() > 2 && tag.charAt(1) != '-') {
                    line.add(tag.substring(0, 2));
                } else if (tag.length() > 2 && tag.charAt(1) == '-') {
                    line.add(String.valueOf(tag.charAt(0)));
                } else {
                    line.add(tag);
                }

                // Features (rest of the tag separated with pipe signs)
                if (conllMap.containsKey(tag)) { // using the map configuration

                    line.add(conllMap.getProperty(tag));

                } else { // tags not listed in the map -- failsafe

                    if (tag.length() > 2 && !tag.contains("unct")) {

                        line.add(Misc.join(tag.substring(2).split(""), "|").substring(1));

                    } else {

                        line.add("_");

                    }

                }
                sentence.add(Misc.join(line, "\t"));

            }

        }

        return conll;
        
    }

    
    /**
     * Converts CLaRK document to a <code>String</code> representation of the 
     * Line data format.
     * 
     * @param   doc CLaRK document
     * @param   eosToken    end of sentence token to be used for separating 
     *                      sentences in the Line data format.
     * @return  {@link String}
     */
    public static String toLine(Document doc, String eosToken) {
        
        StringBuilder lines = new StringBuilder();

        NodeList sentences = doc.getElementsByTagName("s");

        for (int i = 0; i < sentences.getLength(); i++) {
            NodeList tokens = ((Element) sentences.item(i)).getElementsByTagName("tok");

            for (int j = 0; j < tokens.getLength(); j++) {

                Element token = (Element) tokens.item(j);
                lines.append(token.getTextContent().replaceAll(" ", "_"));

                if (token.hasAttribute("ana")) {
                    lines.append(" ");
                    lines.append(token.getAttribute("ana"));
                } else if (token.hasAttribute("svm")) {
                    lines.append(" ");
                    lines.append(token.getAttribute("svm"));
                }

                if (token.hasAttribute("lm")) {
                    lines.append(" ");
                    lines.append(token.getAttribute("lm").replaceAll(" ", "_"));
                }

                lines.append("\n");

            }

            lines.append(eosToken);
            lines.append("\n");

        }

        return lines.toString();
        
    }
    
    
    /**
     * Reads in a CLaRK document from the <code>InputStream</code> into, 
     * converts it to {@link String} in Line data format and writes it into the 
     * <code>OutputStream</code>.
     * 
     * @param   is  <code>InputStream</code> reading a String representation 
     *              of a CLaRK document.
     * @param   os  <OutputStream</code> writing the resulting Line encoded data
     * @param   eosToken    end of sentence token to be used for separating 
     *                      sentences in the Line data format.
     */
    public static void toLine(InputStream is, OutputStream os, String eosToken) {
        
        try {
            os.write(toLine(ClarkDocumentBuilder.buildClarkDocument(is), eosToken).getBytes(ServiceConstants.PIPE_CHARACTER_ENCODING));
        } catch (UnsupportedEncodingException ex) {
            logger.severe(ServiceConstants.EXCEPTION_UNSUPPORTED_ENCODING);
        } catch (IOException ex) {
            logger.severe(ServiceConstants.EXCEPTION_IO);
        }
        
    }

}
