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
package org.bultreebank.labpipe.tools;

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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.apache.commons.lang.StringUtils;
import org.bultreebank.labpipe.data.WebLicht;
import org.bultreebank.labpipe.exceptions.IncorrectInputException;
import org.bultreebank.labpipe.exceptions.MissingContentException;
import org.bultreebank.labpipe.exceptions.SVMTConnectionExceptoin;
import org.bultreebank.labpipe.utils.ClassMap;
import org.bultreebank.labpipe.utils.Configuration;
import org.bultreebank.labpipe.utils.Misc;
import org.bultreebank.labpipe.utils.ServiceConstants;

/**
 * <code>SVMTagger</code> POS-tags text data using an external HTTP server 
 * instance of the SVMTool tagger.
 *
 * @author Aleksandar Savkov
 */
public class SVMTagger {

    /**
     * 
     */
    public static final Logger logger = Logger.getLogger(SVMTagger.class.getName());

    /**
     * Tags Line encoded String
     * 
     * @param   lines   Line encoded data
     * @param   options LABPipe configuration
     * 
     * @return  String  - Line encoded tagged data
     * @throws SVMTConnectionExceptoin
     * @throws IncorrectInputException
     * @throws MissingContentException  
     */
    public static String tagLinesString(String lines, Configuration options) throws SVMTConnectionExceptoin, IncorrectInputException, MissingContentException {
        BufferedReader br = null;
        try {
            StringBuilder sb = new StringBuilder();
            ArrayList<String> sentence = new ArrayList();
            sentence.ensureCapacity(100);
            br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(lines.getBytes(ServiceConstants.PIPE_CHARACTER_ENCODING))));
            String line = "";
            while ((line = br.readLine()) != null) {

                if (line.startsWith(options.getProperty(Configuration.EOS_TOKEN))) {
                    sb.append(Misc.joinColumns(sentence.toArray((String[]) new String[0]), SVMTagger.tagList(sentence, options), " "));
                    sb.append(options.getProperty(Configuration.EOS_TOKEN));
                    sb.append("\n");
                    sentence = new ArrayList();
                    sentence.ensureCapacity(100);
                } else {
                    if (line.trim().length() > 0 && !line.trim().equals("")) {
                        sentence.add(line);
                    }
                }

            }

            if (sentence.size() > 0) {
                sb.append(Misc.joinColumns(sentence.toArray((String[]) new String[0]), SVMTagger.tagList(sentence, options), " "));
                sb.append(options.getProperty(Configuration.EOS_TOKEN));
                sb.append("\n");
            }

            br.close();
            return sb.toString();
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
     * Tags WebLicht stream and outputs it in another stream.
     * 
     * @param   is  input WebLicht
     * @param   os  output WebLicht
     * @param   options LABPipe configuration
     * @throws JAXBException 
     * @throws UnsupportedEncodingException
     * @throws IncorrectInputException
     * @throws SVMTConnectionExceptoin
     * @throws IOException
     * @throws MissingContentException  
     * 
     */
    public static void tagWebLichtStream(InputStream is, OutputStream os, Configuration options) throws JAXBException, IncorrectInputException, SVMTConnectionExceptoin, UnsupportedEncodingException, IOException, MissingContentException {

        WebLicht doc = new WebLicht(is);
        tagWebLicht(doc, options);
        doc.exportAsXML(os);

    }

    /**
     * Tags WebLicht object and creates the <code>tokens</code> element in the same document.
     * 
     * @param   doc WebLicht document
     * @param   options LABPipe configuration
     * @throws IncorrectInputException 
     * @throws MissingContentException
     * @throws SVMTConnectionExceptoin 
     * @throws UnsupportedEncodingException 
     * @throws IOException  
     */
    public static void tagWebLicht(WebLicht doc, Properties options) throws IncorrectInputException, SVMTConnectionExceptoin, UnsupportedEncodingException, IOException, MissingContentException {

        TextCorpus tc = doc.getTextCorpus();
        ClassMap cm = new ClassMap(tc.getTextOrTokensOrSentences());
        ObjectFactory factory = new ObjectFactory();

        if (!cm.containsKey(Tokens.class)) {
            throw new IncorrectInputException(
                    "Missing 'Tokens' component from the WebLicht input during "
                    + "POS tagging.");
        }
        if (!cm.containsKey(Sentences.class)) {
            throw new IncorrectInputException(
                    "Missing 'Sentences' component from the DSpin input "
                    + "during POS tagging.");
        }

        Sentences sentenceListObj = (Sentences) cm.get(Sentences.class);
        List<Sentence> sentenceList = sentenceListObj.getSentence();

        POStags tagListObj = factory.createPOStags();
        List<Tag> tagList = tagListObj.getTag();
        tc.getTextOrTokensOrSentences().add(tagListObj);

        int tagIdx = 1;

        for (Sentence sentence : sentenceList) {
            List<String> tokenListStr = new ArrayList();
            String[] tagArray;
            List<TokenRef> trList = sentence.getTokenRef();
            //tokenListStr.add("Dummie"); // this handles a bug in the SVMT HTTP server or at least I think it is a bug.
            for (TokenRef tr : trList) {
                tokenListStr.add(((Token) tr.getTokID()).getValue().replaceAll(" ", "_"));
            }
            tagArray = tagList(tokenListStr, options);

            if (trList.size() != tagArray.length) {
                logger.severe("tokens: " + trList.size() + " tags: " + tagArray.length);
                logger.info(StringUtils.join(tagArray, " "));
                //WebLichtExporter.exportAsXML(doc, System.out);
                continue;
            }

            for (int i = 0; i < trList.size(); i++) {
                Tag t = factory.createTag();
                t.setValue(tagArray[i]);
                t.setID("tag." + tagIdx);
                t.setTokID(trList.get(i).getTokID());
                tagList.add(t);
                tagIdx++;
            }

        }

    }

    /**
     * Tags a list of strings (ordered usually as a sentence).
     * 
     * @param   input   list of words
     * @param   options LABPipe configuration
     * 
     * @return  String[]    - list of tags in the original word order
     */
    private static String[] tagList(List<String> input, Properties options)
            throws SVMTConnectionExceptoin, UnsupportedEncodingException, IOException, MissingContentException {
        HttpURLConnection connection = null;

        URL serverAddress = null;
        
        if (input.isEmpty()) {
            throw new MissingContentException("No input provided to the SVMTool tagger.");
        }

        String inputStr = "Дъми " + Misc.join(input, " ");
        String response;

        try {
            
            serverAddress = new URL(options.get("svmtUrl")
                    + "?input=" + URLEncoder.encode(inputStr, ServiceConstants.PIPE_CHARACTER_ENCODING));

            //Set up the initial connection
            connection = (HttpURLConnection) serverAddress.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setReadTimeout(1000000);

            connection.connect();

            response = connection.getResponseMessage();

            if (response == null) {
                throw new SVMTConnectionExceptoin("No response from the SVMTool tagger.");
            }
            
            //read the result from the server
            return response.split(" ");

        } catch (MalformedURLException e) {
            throw new SVMTConnectionExceptoin(
                    "SVMT Connection Exception during tagging. "
                    + "Check if the SVMT HTTP server is accessible.");
        } catch (ProtocolException e) {
            throw new SVMTConnectionExceptoin(
                    "SVMT Connection Exception during tagging. "
                    + "Check if the SVMT HTTP server is accessible.");
        } catch (java.net.ConnectException e) {
            throw new SVMTConnectionExceptoin(
                    "SVMT Connection Exception during tagging. "
                    + "Check if the SVMT HTTP server is accessible.");
        } finally {
            //close the connection, set all objects to null
            try {
                connection.disconnect();
            } catch (NullPointerException ex) {
            }
            connection = null;
        }
    }
}
