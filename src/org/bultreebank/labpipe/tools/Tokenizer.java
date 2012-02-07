/*
 * LABPipe - Natural Language Processing Pipeline for Bulgarian
 * Copyright (C) 2011 Institute for Information and Communication Technologies
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
import de.dspin.data.textcorpus.Sentence;
import de.dspin.data.textcorpus.Sentences;
import de.dspin.data.textcorpus.TextCorpus;
import de.dspin.data.textcorpus.Token;
import de.dspin.data.textcorpus.TokenRef;
import de.dspin.data.textcorpus.Tokens;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.bultreebank.labpipe.data.WebLicht;
import org.bultreebank.labpipe.exceptions.IncorrectInputException;
import org.bultreebank.labpipe.utils.ClassMap;
import org.bultreebank.labpipe.utils.ServiceConstants;

/**
 * <code>Tokenizer</code> is a general purpose tokenizer class
 *
 * @author Aleksandar Savkov
 */
public abstract class Tokenizer {
    
    private HashMap ESCAPES = null;
    /**
     * Tokenizes String text into Line encoded data
     * 
     * @param   text    text to be tokenized
     * 
     * @return  String  - Line encoded data
     */
    public abstract String tokenize(String text) throws IOException, InterruptedException;
    
    /**
     * Tokenizes a WebLicht file containing only <code>text</code> element and produces a {@link WebLicht} object.
     * 
     * @param   file    WebLicht file
     * 
     * @return  {@link WebLicht}    - tokenized object
     */
    public abstract WebLicht tokenize(File file) throws IOException, InterruptedException, FileNotFoundException, IncorrectInputException;
    
    /**
     * Tokenizes a WebLicht object
     * 
     * @param   doc WebLicht object
     * 
     * @return  {@link WebLicht}
     */
    public WebLicht tokenize(WebLicht doc) throws IncorrectInputException, IOException, InterruptedException {
        List tcList = doc.getTextCorpus().getTextOrTokensOrSentences();
        ClassMap cm = new ClassMap(tcList);
        if (!cm.containsKey(String.class)) {
            throw new IncorrectInputException(
                    "Empty or missing text element in the CorpusText element.");
        }
        
        String text = cm.get(String.class).toString();
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(tokenize(text).getBytes(ServiceConstants.PIPE_CHARACTER_ENCODING))));

        ObjectFactory factory = new ObjectFactory();
        Tokens tlObj = factory.createTokens();
        List<Token> tl = tlObj.getToken();
        Sentences slObj = factory.createSentences();
        List<Sentence> sl = slObj.getSentence();
        TextCorpus tc = doc.getTextCorpus();
        List tcl = tc.getTextOrTokensOrSentences();
        tcl.add(tlObj);
        tcl.add(slObj);

        int tokIdx = 1;
        int charIdx = 0;
        int sentIdx = 1;
        
        Token token = null;
        List<TokenRef> trList;
        TokenRef tr = factory.createTokenRef();
        Sentence sentence = factory.createSentence();
        sentence.setID("snt.1");
        sentence.setStart(1);
        trList = sentence.getTokenRef();
        
        HashMap<Token,List<String>> escapes = new HashMap();
        
        String line = null;

        while ((line = br.readLine()) != null) {
            if (line.matches(ServiceConstants.ESCAPE_SEQUENCE_MODEL)) {
                if (escapes.containsKey(token)) {
                    escapes.get(token).add(line);
                } else {
                    List<String> list = new ArrayList();
                    list.add(line);
                    escapes.put(token, list); // when the first token is an escape sequence the key is null.
                }
            } else if (line.contains("<eos>")) {
                sentence.setEnd(tokIdx - 1);
                sl.add(sentence);
                sentence = factory.createSentence();
                sentIdx++;
                sentence.setID("sent" + String.valueOf(sentIdx));
                sentence.setStart(tokIdx);
                trList = sentence.getTokenRef();
            } else if (line.contains("<eof>") || line.length() == 0) {
                break;
            } else {
                token = factory.createToken();
                token.setID("tok." + String.valueOf(tokIdx));
                token.setStart(charIdx);
                token.setEnd(charIdx + line.length());
                token.setValue(line.trim());
                tl.add(token);
                tr = factory.createTokenRef();
                tr.setTokID(token);
                trList.add(tr);
                
                tokIdx++;
                charIdx += (line.length() + 1);
            }
        }
        
        if (trList.size() > 0) {
            sentence.setEnd(tokIdx - 1);
            sl.add(sentence);
        }

        br.close();
        
        ESCAPES = escapes;
        
        return doc;
        
    }
    
    /**
     * Tokenizes <code>InputStream</code> of WebLicht document and prints the new WebLicht document into the <code>OutputStream</code>
     * 
     * @param   is  WebLicht <code>InputStream</code>
     * @param   os  WebLicht <code>OutputStream</code>
     */
    public WebLicht tokenize(InputStream is, OutputStream os) throws JAXBException, IncorrectInputException, InterruptedException, IOException {
        
        JAXBContext jc = JAXBContext.newInstance("de.dspin.data");
        WebLicht doc = new WebLicht(is);
        
        tokenize(doc);
        
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(doc, os);
        
        return doc;
        
    }
    
    /**
     * Retrieves the escapes <code>Map</code>
     * 
     * @return  HashMap - escapes
     */
    public HashMap getEscapes() {
        return ESCAPES;
    }

}
