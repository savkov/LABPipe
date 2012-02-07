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
package org.bultreebank.labpipe.tools;

import de.dspin.data.textcorpus.ObjectFactory;
import de.dspin.data.textcorpus.Sentence;
import de.dspin.data.textcorpus.Sentences;
import de.dspin.data.textcorpus.TextCorpus;
import de.dspin.data.textcorpus.Token;
import de.dspin.data.textcorpus.TokenRef;
import de.dspin.data.textcorpus.Tokens;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.bultreebank.labpipe.data.WebLicht;
import org.bultreebank.labpipe.tools.options.SentenceBoundaryOptions;
import org.bultreebank.labpipe.exceptions.EmptyConfigFileException;
import org.bultreebank.labpipe.exceptions.IncorrectInputException;
import org.bultreebank.labpipe.utils.ClassMap;
import org.bultreebank.labpipe.utils.Configuration;
import org.bultreebank.labpipe.utils.ServiceConstants;

/**
 *
 * @deprecated since v1.0
 * @author Aleksandar Savkov
 */
public class SentenceDetector {

    private Configuration OPTIONS = null;
    private SentenceBoundaryOptions SBO = new SentenceBoundaryOptions();
    private final Logger log = Logger.getLogger(getClass().getName());

    public SentenceDetector(Configuration options) {
        
        OPTIONS = options;
        
        try {
            if (options.size() == 0) {
                throw new EmptyConfigFileException();
            }
            SBO.setBoundaryToken((String) options.get("boundaryToken"));
            SBO.setBoundarySigns((String) options.get("boundarySigns"));
            SBO.setPossibleEosDict(readDict((String) options.get("possibleEOS")));
            SBO.setNamesDictionary(readDict((String) options.get("namesDictionary")));
            SBO.setAverageSentenceLength(
                    Integer.parseInt((String) options.get("averageSentenceLength")));
        } catch (NullPointerException ex) {
            System.err.println("One or more of the required arguments are "
                    + "missing from the sentence boundary configuration file!");
            log.log(Level.SEVERE, "One or more of the required arguments are "
                    + "missing from the sentence boundary configuration file!",
                    ex);
            System.exit(1);
        } catch (EmptyConfigFileException ex) {
            System.err.println("No arguments provided in the sentence "
                    + "boundary configuration file!");
            log.log(Level.SEVERE, "No arguments provided in the sentence "
                    + "boundary configuration file!", ex);
        }
        
    }

    public SentenceDetector(String boundaryToken, String boundarySigns, String possibleEosPath,
            String namesDictPath, int avgSentLength) {
        SBO.setBoundarySigns(boundarySigns);
        SBO.setNamesDictionary(readDict(namesDictPath));
        SBO.setAverageSentenceLength(avgSentLength);
        SBO.setBoundaryToken(boundaryToken);
    }
    
    public WebLicht detectSentenceBoundaries(WebLicht doc) throws IncorrectInputException {
        
        TextCorpus tc = doc.getTextCorpus();
        List tcl = tc.getTextOrTokensOrSentences();
        ClassMap cm = new ClassMap(tcl);
        
        if (!cm.containsKey(Tokens.class)) {
            
            throw new IncorrectInputException();
            
        }
        
        tcl.add(detectSentenceBoundaries((Tokens)cm.get(Tokens.class)));
        
        return doc;
        
    }

    public Sentences detectSentenceBoundaries(Tokens tokens) {

        int lastEosIndex = -1;

        ObjectFactory factory = new ObjectFactory();
        Sentences sentences = factory.createSentences();
        List<Sentence> sentenceList = sentences.getSentence();
        ArrayList<Token> tokenList = new ArrayList(tokens.getToken());

        ListIterator<Token> itr = tokenList.listIterator();

        Sentence sentence = factory.createSentence();
        sentence.setStart(1);

        while (itr.hasNext() && itr.nextIndex() + 1 < tokenList.size()) {
            Token token = itr.next();
            String tokenStr = token.getValue();
            TokenRef tr = factory.createTokenRef();
            tr.setTokID(token);
            sentence.getTokenRef().add(tr);
            int index = itr.nextIndex();
            boolean isBoundarySign = tokenStr.matches(SBO.getBoundarySigns() + "+");
            boolean isPossibleEos = SBO.getPossibleEosDict().contains(tokenStr);
            boolean nextTokenIsCapitalized = tokenList.get(itr.nextIndex()).getValue().matches("[A-ZА-Я0-9].*");
            boolean nextTokenIsNotName = !SBO.getDictionary().contains(tokenList.get(itr.nextIndex()).getValue());
            boolean isSentenceOfAverageLength =
                    (index - lastEosIndex >= SBO.getAverageSentenceLength())
                    ? true : false;
            if (nextTokenIsCapitalized && (isBoundarySign | isPossibleEos)
                    && (nextTokenIsNotName | isSentenceOfAverageLength)) {
                sentence.setEnd(index);
                sentenceList.add(sentence);
                sentence = factory.createSentence();
                sentence.setStart(index + 1);
                lastEosIndex = index;
            }
        }

        if (lastEosIndex
                != tokenList.size()) {
            sentence.setEnd(tokenList.size());
        }

        return sentences;
    }

    public void detectSentenceBoundaries(InputStream is, OutputStream os) throws JAXBException, IncorrectInputException {

        JAXBContext jc = JAXBContext.newInstance("de.dspin.data");
        WebLicht doc = new WebLicht(is);
        TextCorpus tc = doc.getTextCorpus();
        List tcList = tc.getTextOrTokensOrSentences();
        ClassMap cm = new ClassMap(tcList);

        if (cm.containsKey(String.class) && cm.containsKey(Tokens.class)) {
            tcList.add(detectSentenceBoundaries((Tokens) cm.get(Tokens.class)));
        } else {
            if (cm.containsKey(String.class)) {
                throw new IncorrectInputException(
                        "Empty or missing Tokens element in the CorpusText "
                        + "element.");

            } else {
                throw new IncorrectInputException(
                        "Empty or missing text element in the CorpusText "
                        + "element.");
            }
        }
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(doc, os);
        
    }

    private Set<String> readDict(String filePath) {
        Set<String> dict = new TreeSet();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(OPTIONS.getStoreDirPath() + filePath), ServiceConstants.PIPE_CHARACTER_ENCODING));
            String line;
            while ((line = br.readLine()) != null) {
                dict.add(line);
            }
        } catch (IOException ex) {
            System.err.println("Cannot access a dictionary file: " + ex.getMessage());
            log.log(Level.SEVERE, "Cannot access a dictionary file: " + ex.getMessage(), ex);
            System.exit(1);
        }
        return dict;
    }
    
}
