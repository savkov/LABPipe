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

import de.dspin.data.textcorpus.Lemma;
import de.dspin.data.textcorpus.Lemmas;
import de.dspin.data.textcorpus.ObjectFactory;
import de.dspin.data.textcorpus.POStags;
import de.dspin.data.textcorpus.Sentence;
import de.dspin.data.textcorpus.Sentences;
import de.dspin.data.textcorpus.Tag;
import de.dspin.data.textcorpus.TextCorpus;
import de.dspin.data.textcorpus.Token;
import de.dspin.data.textcorpus.TokenRef;
import de.dspin.data.textcorpus.Tokens;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.bultreebank.labpipe.data.ClarkDocumentBuilder;
import org.bultreebank.labpipe.data.Conll;
import org.bultreebank.labpipe.data.WebLicht;
import org.bultreebank.labpipe.utils.DataUtils;
import org.bultreebank.labpipe.utils.Misc;
import org.bultreebank.labpipe.utils.ServiceConstants;
import org.bultreebank.labpipe.utils.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <code>LineConverter</code> converts Line encoded data to the other 
 * <code>LABPipe</code> data formats: <code>Gaze</code>, {@link Conll}, 
 * <code>CLaRK</code> and {@link WebLicht}.
 *
 * @author Aleksandar Savkov
 */
public class LineConverter {

    private static final Logger logger = Logger.getLogger(LineConverter.class.getName());

    /**
     * Reads Line encoded data from the <code>InputStream</code> into and 
     * returns a {@link Conll} object containing its <code>String</code> 
     * representation.
     * 
     * @param   is  <code>InputStream</code> reading a Line encoded data.
     * @param   eosToken   end of sentence token
     * @param   conllMap    <code>Map</code> containing connections between full
     *                      BTB tags and their respective CoNLL forms.
     * @return  {@link Conll}
     * @throws UnsupportedEncodingException 
     * @throws FileNotFoundException
     * @throws IOException  
     */
    public static Conll toConll(InputStream is, String eosToken, Properties conllMap)
            throws UnsupportedEncodingException,
            FileNotFoundException, IOException {

        Conll conll = new Conll();
        ArrayList<String> conllSentence = new ArrayList();
        StringBuilder conllLine = new StringBuilder();

        BufferedReader br = new BufferedReader(new InputStreamReader(is, ServiceConstants.PIPE_CHARACTER_ENCODING));
        String line = null;
        int id = 1;

        while ((line = br.readLine()) != null) {

            if (!line.trim().startsWith(eosToken)) {

                try {
                    conllLine.append(DataUtils.lineTokenToConllToken(line, id, conllMap));
                    conllLine.append("\n");
                } catch (ArrayIndexOutOfBoundsException ex) {
                    id--;
                }
                id++;
                conllSentence.add(conllLine.toString());

            } else {

                id = 1;
                conllLine.append("\n");
                conllSentence.add(conllLine.toString());
                conll.add(conllSentence);

            }


            conllLine.setLength(0);

        }

        br.close();

        return conll;

    }

    /**
     * Reads Line encoded data from the <code>InputStream</code> into, converts
     * it to {@link Conll} object and writes its <code>String</code> 
     * representation into the <code>OutputStream</code>.
     * 
     * @param   is  <code>InputStream</code> reading a Line encoded data.
     * @param   os  <OutputStream</code> writing the <code>String</code> 
     *              representation of the resulting <code>CoNLL</code> object.
     * @param   eosToken   end of sentence token
     * @param   conllMap    <code>Map</code> containing connections between full
     *                      BTB tags and their respective CoNLL forms.
     */
    public static void toConll(InputStream is, OutputStream os, String eosToken, Properties conllMap) {

        try {

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));

            String line;
            int num = 1;

            while ((line = br.readLine()) != null) {

                line = line.trim();

                if (line.startsWith("##") || (!line.contains(" ") && !line.contains(eosToken))) {
                    continue;
                }
                if (line.contains(eosToken)) {
                    num = 1;
                    bw.newLine();
                } else {
                    bw.write(DataUtils.lineTokenToConllToken(line, num, conllMap));
                    bw.newLine();
                    num++;
                }

            }
            bw.close();
            br.close();

        } catch (IOException ex) {
            logger.severe(ServiceConstants.EXCEPTION_IO);
        }

    }

    /**
     * Returns a converted Line encoded data from a <code>String</code> to a 
     * {@link Conll} object.
     * 
     * @param   lines  Line encoded data
     * @param   eosToken   end of sentence token
     * @param   conllMap    <code>Map</code> containing connections between full
     *                      BTB tags and their respective CoNLL forms.
     * @return  {@link Conll}
     */
    public static Conll toConll(String lines, String eosToken, Properties conllMap) {

        Conll conll = new Conll();

        int num = 1;

        ArrayList<String> sentence = new ArrayList(100);

        for (String line : lines.split("\n")) {

            line = line.trim();

            if (line.startsWith("##") || (!(line.contains(" ")||line.contains("\t")) && !line.contains(eosToken))) {
                continue;
            }
            if (line.contains(eosToken)) {
                num = 1;
                sentence.trimToSize();
                conll.add(sentence);
                sentence = new ArrayList(100);
            } else {
                sentence.add(DataUtils.lineTokenToConllToken(line, num, conllMap));
                num++;
            }

        }

        sentence.trimToSize();

        if (sentence.size() > 0) {

            conll.add(sentence);

        }

        conll.trimToSize();

        return conll;
    }

    /**
     * Converts a Line encoded token into a CoNLL encoded token.
     * 
     * @param   line    Line encoded token
     * @param   id      ID of the token in CoNLL representation
     * @param   conllMap    <code>Map</code> containing connections between full
     *                      BTB tags and their respective CoNLL forms.
     * @return  {@link String} - CoNll encoded token
     * @throws ArrayIndexOutOfBoundsException  
     */
    public static String lineTokenToConllToken(String line, int id, Properties conllMap)
            throws ArrayIndexOutOfBoundsException {

        ArrayList<String> conll = new ArrayList(10);

        line = line.replaceAll(" ", "\t");

        String[] columns = line.split("\t");

        String token = columns[0];
        String tag = columns[1];
        String lemma = null;
        if (columns.length > 2) {
            lemma = columns[2];
        }

        // ID number
        conll.add(String.valueOf(id));

        // Token
        conll.add(token);

        // Lemma
        if (lemma != null) {
            conll.add(lemma);
        } else {
            conll.add("_");
        }

        // Short tag (BTB first letter)
        if (tag.contains("punct")) {
            conll.add("Punct");
        } else {
            conll.add(String.valueOf(tag.charAt(0)));
        }

        // Long tag
        if (tag.contains("punct") || tag.contains("Punct")) {
            conll.add("Punct");
        } else if (tag.length() > 2 && tag.charAt(1) != '-') {
            conll.add(tag.substring(0, 2));
        } else if (tag.length() > 2 && tag.charAt(1) == '-') {
            conll.add(String.valueOf(tag.charAt(0)));
        } else {
            conll.add(tag);
        }

        // Features (rest of the tag separated with pipe signs)
        if (conllMap.containsKey(tag)) { // using the map configuration

            conll.add(conllMap.getProperty(tag));

        } else { // tags not listed in the map -- failsafe

            if (tag.length() > 2 && !tag.contains("unct")) {

                conll.add(Misc.join(tag.substring(2).split(""), "|").substring(1));

            } else {

                conll.add("_");

            }

        }

        return Misc.join(conll, "\t");

    }
    
    /**
     * @throws ArrayIndexOutOfBoundsException 
     * @deprecated since v1.0
     */
    public static String toConllLine(String line, int id, Properties conllMap)
            throws ArrayIndexOutOfBoundsException {

        StringBuilder conll = new StringBuilder();

        line = line.replaceAll(" ", "\t");

        String[] columns = line.split("\t");

        String token = columns[0];
        String tag = columns[1];
        String lemma = null;
        if (columns.length > 2) {
            lemma = columns[2];
        }

        // ID number
        conll.append(id);
        conll.append("\t");

        // Token
        conll.append(token);
        conll.append("\t");

        // Lemma
        if (lemma != null) {
            conll.append(lemma);
        } else {
            conll.append("_");
        }
        conll.append("\t");

        // Short tag (BTB first letter)
        if (tag.contains("punct")) {
            conll.append("Punct");
        } else {
            conll.append(tag.charAt(0));
        }
        conll.append("\t");

        // Long tag
        if (tag.contains("punct") || tag.contains("Punct")) {
            conll.append("Punct");
        } else if (tag.length() > 2 && tag.charAt(1) != '-') {
            conll.append(tag.substring(0, 2));
        } else if (tag.length() > 2 && tag.charAt(1) == '-') {
            conll.append(tag.charAt(0));
        } else {
            conll.append(tag);
        }
        conll.append("\t");

        // Features (rest of the tag separated with pipe signs)
        if (conllMap.containsKey(tag)) { // using the map configuration

            conll.append(conllMap.getProperty(tag));

        } else { // tags not listed in the map -- failsafe

            if (tag.length() > 2 && !tag.contains("unct")) {

                conll.append(StringUtils.join(tag.substring(2).split(""), "|").substring(1));

            } else {

                conll.append("_");

            }

        }

        return conll.toString();

    }
    
    /**
     * Converts Line encoded data from the <code>InputStream</code> to Gaze data
     *  in the <code>OutputStream</code>.
     * 
     * @param   is  <code>InputStream</code> reading a Line encoded data.
     * @param   os  <OutputStream</code> writing <code>Gaze</code> encoded data.
     * @param   eosToken   end of sentence token
     * 
     */
    public static void toGaze(InputStream is, OutputStream os, String eosToken) {
        
        try {
            Misc.writeStream(
                    new ByteArrayInputStream(toGaze(Misc.readInputStream(is), eosToken).getBytes(ServiceConstants.PIPE_CHARACTER_ENCODING)), os);
        } catch (UnsupportedEncodingException ex) {
            logger.severe(ServiceConstants.EXCEPTION_UNSUPPORTED_ENCODING);
        }
        
    }
    
    /**
     * Converts Line encoded data <code>String</code> to Gaze data.
     * 
     * @param   lines   Line encoded data.
     * @param   eosToken   end of sentence token
     * 
     * @return  {@link String} - Gaze data
     */
    public static String toGaze(String lines, String eosToken) {
        String[] linesAr = lines.split("\n");
        StringBuilder sb = new StringBuilder();

        for (String line : linesAr) {
            line = line.replace("\t", " ");
            String[] lineItems = line.split(" ");
            if (lineItems.length < 2 || lines.equals(eosToken)) {
                sb.append(ServiceConstants.GAZE_EOS_TOKEN);
                sb.append("\n");
                continue;
            }
            sb.append(lineItems[0]);
            sb.append("\t");
            sb.append(lineItems[1]);
            sb.append("\t");
            sb.append("O");
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Converts Line encoded data into a CLaRK document.
     * 
     * @param   lines   Line encoded data
     * @param   eosToken   end of sentence token
     * 
     * @return  {@link org.w3c.dom.Document} - CLaRK Document
     */
    public static Document toClark(String lines, String eosToken) {
        try {

            Document clarkDoc = ClarkDocumentBuilder.buildClarkDocument();

            BufferedReader br = new BufferedReader(new StringReader(lines));

            String line;

            Element root = clarkDoc.createElement("root");
            Element sentence = clarkDoc.createElement("s");
            Element token = null;
            String[] lineSegments;
            clarkDoc.appendChild(root);

            while ((line = br.readLine()) != null) {

                if (line.contains(eosToken)) {

                    root.appendChild(sentence);
                    sentence = clarkDoc.createElement("s");

                } else {

                    token = clarkDoc.createElement("tok");
                    line = line.replaceAll("\t", " ");
                    lineSegments = line.split(" ");
                    token.setTextContent(lineSegments[0]);
                    if (lineSegments.length > 1) {
                        token.setAttribute("svm", lineSegments[1]);
                    }
                    if (lineSegments.length > 2) {
                        token.setAttribute("lm", lineSegments[2]);
                    }
                    sentence.appendChild(token);

                }

            }

            return clarkDoc;

        } catch (IOException ex) {
            logger.severe(ServiceConstants.EXCEPTION_IO);
        }

        return null;
    }

    /**
     * Converts Line encoded data <code>InputStream</code> into a CLaRK document
     *  and prints it into the <code>OutputStream</code>.
     * 
     * @param   is  Line encoded data <code>InputStream</code>
     * @param   os  <code>OutputStream</code> containing the CLaRK document representation.
     * @param   eosToken    end of sentence token
     * 
     */
    public static void toClark(InputStream is, OutputStream os, String eosToken) {
        
        Document doc = toClark(Misc.readInputStream(is), eosToken);
        XmlUtils.print(doc, os);
        
    }
    
    /**
     * Converts Line encoded data <code>InputStream</code> into a 
     * {@link WebLicht} document.
     * 
     * @param   is  Line encoded data <code>InputStream</code>
     * @param   eosToken    end of sentence token
     * @param   doc {@link WebLicht} document receiving the data
     * 
     * @return {@link WebLicht}
     * 
     */
    public static WebLicht toWebLicht(InputStream is, String eosToken, WebLicht doc) {
        
        BufferedReader br = null;
        
        try {
            br = new BufferedReader(new InputStreamReader(is, ServiceConstants.PIPE_CHARACTER_ENCODING));
            ObjectFactory factory = new ObjectFactory();
            TextCorpus tc = factory.createTextCorpus();
            doc.setTextCorpus(tc);
            List tcList = tc.getTextOrTokensOrSentences();
            Tokens tokens = factory.createTokens();
            List<Token> tokenList = tokens.getToken();
            POStags tags = factory.createPOStags();
            List<Tag> tagList = tags.getTag();
            Lemmas lemmas = factory.createLemmas();
            List<Lemma> lemmaList = lemmas.getLemma();
            Sentences sentences = factory.createSentences();
            List<Sentence> sentenceList = sentences.getSentence();
            int index = 0;
            int id = 1;
            int sId = 1;
            String textStr = "";
            String line;
            Sentence sentence = factory.createSentence();
            sentence.setID("sent" + sId);
            sentence.setStart(id);
            while ((line = br.readLine()) != null) {
                Token token = factory.createToken();
                Tag tag = factory.createTag();
                Lemma lemma = factory.createLemma();
                Matcher m = Pattern.compile("[^\\s\\t]+").matcher(line);
                Matcher m2 = Pattern.compile(
                        "([^\\s\\t]+)(?:\\s+|\\t+)([^\\s\\t]+)").matcher(line);
                Matcher m3 = Pattern.compile(eosToken).matcher(line);
                Matcher m4 = Pattern.compile(
                        "([^\\s\\t]+)(?:\\s+|\\t+)([^\\s\\t]+)(?:\\s+|\\t+)([^\\s\\t]+)").matcher(line);

                if (m3.find()) {
                    sentence.setEnd(id - 1);
                    sentenceList.add(sentence);
                    sentence = factory.createSentence();
                    sId++;
                    sentence.setID("sent" + sId);
                    sentence.setStart(id);
                    continue;
                } else if (m.matches()) {
                    token.setValue(m.group());
                } else if (m4.matches()) {
                    token.setValue(m4.group(1));
                    tag.setValue(m4.group(2));
                    tag.setTokID(token);
                    tag.setID("tag." + id);
                    lemma.setValue(m4.group(3));
                    lemma.setTokID(token);
                    lemma.setID("lem." + id);
                } else if (m2.matches()) {
                    token.setValue(m2.group(1));
                    tag.setValue(m2.group(2));
                    tag.setTokID(token);
                    tag.setID("tag." + id);
                } else {
                    continue;
                }

                TokenRef tr = factory.createTokenRef();
                tr.setTokID(token);
                sentence.getTokenRef().add(tr);

                if (!token.getValue().matches(ServiceConstants.PRE_SPACE_PUNCT_SIGNS_PATTERN)) {
                    textStr = textStr.concat(" ");
                }
                textStr = textStr.concat(token.getValue());
                index = textStr.length();
                token.setID("tok." + id);
                token.setStart(index - token.getValue().length());
                token.setEnd(index - 1);
                tokenList.add(token);
                if (tag.getValue() != null) {
                    tagList.add(tag);
                }
                if (lemma.getValue() != null) {
                    lemmaList.add(lemma);
                }
                id++;

            }
            if (sentence.getTokenRef().size() > 0) {
                if (sentence.getEnd() == null) {
                    sentence.setEnd(sentence.getTokenRef().size());
                }
                sentenceList.add(sentence);
            }
            tcList.add(textStr);
            tcList.add(tokens);
            if (lemmaList.size() > 0) {
                tcList.add(lemmas);
            }
            tcList.add(tags);
            tcList.add(sentences);
            return doc;

        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_UNSUPPORTED_ENCODING, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_IO, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_IO, ex);
            }
        }

        return null;
    }
    
    /**
     * Returns a {@link WebLicht} document built from the Line encoded data 
     * from the <code>InputStream</code>.
     * 
     * @param   is  Line encoded data <code>InputStream</code>
     * @param   eosToken    end of sentence token
     * 
     * @return {@link WebLicht}
     * 
     */
    public static WebLicht toWebLicht(InputStream is, String eosToken) {
        
        WebLicht doc = new WebLicht();
        return toWebLicht(is, eosToken, doc);

    }
    
}
