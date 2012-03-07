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
package org.bultreebank.labpipe.utils;

import de.dspin.data.textcorpus.Analysis;
import de.dspin.data.textcorpus.Dependency;
import de.dspin.data.textcorpus.Depparsing;
import de.dspin.data.textcorpus.Lemma;
import de.dspin.data.textcorpus.Lemmas;
import de.dspin.data.textcorpus.POStags;
import de.dspin.data.textcorpus.Sentence;
import de.dspin.data.textcorpus.Tag;
import de.dspin.data.textcorpus.Token;
import de.dspin.data.textcorpus.TokenRef;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.bultreebank.labpipe.data.Conll;

/**
 * <code>DataUtils</code> utilizes processes related to data the formats and object in LABPipe.
 *
 * @author Aleksandar Savkov
 */
public class DataUtils {
    
    private static final Logger logger = Logger.getLogger(DataUtils.class.getName());
    
    /**
     * Finds the correct object referencing the <code>ref</code> in a list of 
     * data objects used in DSpin objects. For example, finds a <code>Tag</code>
     * referencing  a <code>Token</code> in a <code>List&lt;Token&gt;</code>.
     * 
     * @param   list    data objects list
     * @param   ref     reference object
     * 
     * @return  Object  - referencing object
     */
    public static Object getBackRef(List list, Object ref) {

        if (ref == null || list == null) {
            return null;
        }

        for (Object item : list) {

            if (item.getClass().getName().equals(TokenRef.class.getName())) {
                TokenRef tr = (TokenRef) item;
                if (tr != null && tr.getTokID() != null
                        && tr.getTokID().equals(ref)) {
                    return item;
                }
            } else if (item.getClass().getName().equals(Lemma.class.getName())) {
                Lemma lemma = (Lemma) item;
                if (lemma != null && lemma.getTokID() != null
                        && lemma.getTokID().equals(ref)) {
                    return item;
                }
            } else if (item.getClass().getName().equals(Tag.class.getName())) {
                Tag tag = (Tag) item;
                if (tag != null && tag.getTokID() != null
                        && tag.getTokID().equals(ref)) {
                    return item;
                }
            } else if (item.getClass().getName().equals(Analysis.class.getName())) {
                Analysis analysis = (Analysis) item;
                if (analysis != null && analysis.getTokID() != null
                        && analysis.getTokID().equals(ref)) {
                    return item;
                }
            } else if (item.getClass().getName().equals(Dependency.class.getName())) {
                Dependency dep = ((Dependency) item);
                if (dep != null && dep.getDepID() != null
                        && dep.getDepID().equals(ref)) {
                    return item;
                }
            } else if (item.getClass().getName().equals(Depparsing.Parse.class.getName())) {
                List parseList = ((Depparsing.Parse) item).getDependency();
                Dependency dependency = (Dependency) getBackRef(parseList, ref);
                if (dependency != null) {
                    return item;
                }
            }

        }

        return null;

    }
    
    /**
     * Converts a <code>DSpin Sentence</code> object into a 
     * <code>ArrayList&lt;String&gt;</code> that can be used in constructing 
     * {@link Conll} objects.
     * 
     * @param   sentence    <code>DSpin Sentence</code> object
     * @param   cm          {@link ClassMap} object
     * @param   conllMap    <code>Map</code> linking BTB tags to their CoNLL representation forms (features)
     * 
     * @return  ArrayList&lt;String&gt;
     */
    public static ArrayList<String> dspinSentAsConllArray(Sentence sentence, ClassMap cm, Properties conllMap) {
        
        ArrayList<String> conllSentence = new ArrayList();
        
        List<TokenRef> tokRefs = sentence.getTokenRef();
        
        int id = 1;
        
        StringBuilder conllLine;
        
        Lemmas lemmas = (Lemmas) cm.get(Lemmas.class);
        Token token = null;
        Lemma lemma = null;
        Tag tagXml = null;
        String tag = null;
        Depparsing.Parse parse = null;
        Dependency dependency = null;
        HashMap tokenIndex = new HashMap();

        try {
            for (TokenRef tokRef : tokRefs) {

                conllLine = new StringBuilder();

                // Id
                conllLine.append(id);
                conllLine.append("\t");
                id++;

                // Token
                token = (Token) tokRef.getTokID();
                conllLine.append(token.getValue());
                tokenIndex.put(token, new Integer(id - 1));
                conllLine.append("\t");

                // Lemma
                if (lemmas != null) {
                    lemma = (Lemma) DataUtils.getBackRef(lemmas.getLemma(), token);
                    if (lemma != null && lemma.getValue().length() > 0) {
                        conllLine.append(lemma.getValue().replaceAll(" ", "_"));
                    } else {
                        conllLine.append("_");
                    }
                } else {
                    conllLine.append("_");
                }
                conllLine.append("\t");


                if (cm.get(POStags.class) != null) {

                    tagXml = (Tag) DataUtils.getBackRef(((POStags) cm.get(POStags.class)).getTag(), token);
                    tag = tagXml.getValue();

                    // Short tag (BTB first letter)
                    if (tag.contains("punct")) {
                        conllLine.append("Punct");
                    } else {
                        conllLine.append(tag.charAt(0));
                    }
                    conllLine.append("\t");

                    // Long tag
                    if (tag.contains("punct") || tag.contains("Punct")) {
                        conllLine.append("Punct");
                    } else if (tag.startsWith("V")) {
                        conllLine.append(tag.substring(0, 3));
                    } else if (tag.length() > 2 && tag.charAt(1) != '-') {
                        conllLine.append(tag.substring(0, 2));
                    } else if (tag.length() >= 2 && tag.charAt(1) == '-') {
                        conllLine.append(tag.charAt(0));
                    } else {
                        conllLine.append(tag);
                    }
                    conllLine.append("\t");


                    // Features (rest of the tag separated with pipe signs)
                    if (conllMap.containsKey(tag)) { // using the map configuration

                        conllLine.append(conllMap.getProperty(tag));

                    } else { // tags not listed in the map -- failsafe

                        if (tag.length() > 2 && !tag.contains("unct")) {

                            conllLine.append(StringUtils.join(tag.substring(2).split(""), "|").substring(1));

                        } else {

                            conllLine.append("_");

                        }

                    }

                } else {
                    conllLine.append("_\t_\t_");
                }
                conllLine.append("\t");


                // Dependency Parsing
                if (cm.containsKey(Depparsing.class)) {

                    List parseList = ((Depparsing) cm.get(Depparsing.class)).getParse();

                    if (!parseList.isEmpty()) {

                        parse = (Depparsing.Parse) DataUtils.getBackRef(parseList, token);

                        if (parse != null
                                && parse.getID().substring(3).equals(sentence.getID().substring(3))) {

                            dependency = (Dependency) DataUtils.getBackRef(parse.getDependency(), token);

                            conllLine.append(tokenIndex.get(dependency.getGovID()));
                            conllLine.append("\t");
                            conllLine.append(dependency.getFunc());
                            conllLine.append("\t");

                        }

                    }

                }

                conllSentence.add(conllLine.toString());

            }
        } catch (NullPointerException ex) {
            logger.log(Level.SEVERE, "Error occurred during parsing of sentence number ".concat((id - 1) + ""), ex);
        }

        return conllSentence;

    }
    
    /**
     * Converts a token encoded in Line data format into CoNLL
     * 
     * @param   line    Line encoded token
     * @param   id      ID of the current token
     * @param   conllMap    <code>Map</code> linking BTB tags to their CoNLL representation forms (features)
     * 
     * @return  String  - CoNLL encoded token
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
            if (tag.startsWith("V")) {
                conll.add(tag.substring(0, 3));
            } else {
                conll.add(tag.substring(0, 2));
            }
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
     * Builds a <code>HashMap</code> from a CoNLL line (token).
     * 
     * @param   line    CoNLL line (token)
     * @param   iConllMap   inverted CoNLL <code>Map</code> that links long POS 
     *                      tags and features to original BTB tags.
     * 
     * @return HashMap&ltInteger, String&gt;
     */
    public static HashMap<Integer, String> conllLineAsMap(String line, Properties iConllMap) {

        String splitter = (line.contains("\t")) ? "\t" : " ";
        HashMap<Integer, String> map = new HashMap();
        String[] tokenArray = line.split(splitter);

        map.put(Conll.TOKEN_INDEX, tokenArray[Conll.TOKEN_INDEX]);
        map.put(Conll.TOKEN_FORM, tokenArray[Conll.TOKEN_FORM]);
        map.put(Conll.TOKEN_LEMMA, tokenArray[Conll.TOKEN_LEMMA]);
        map.put(Conll.TOKEN_CPOSTAG, tokenArray[Conll.TOKEN_CPOSTAG]);
        map.put(Conll.TOKEN_POSTAG, tokenArray[Conll.TOKEN_POSTAG]);
        map.put(Conll.TOKEN_FEATS, tokenArray[Conll.TOKEN_FEATS]);
        String fullTag = (map.get(Conll.TOKEN_FEATS).equals("_") || map.get(Conll.TOKEN_FEATS) == null) ? map.get(Conll.TOKEN_POSTAG) : iConllMap.getProperty(map.get(Conll.TOKEN_POSTAG) + map.get(Conll.TOKEN_FEATS));
        if (fullTag == null) {
            System.out.println(iConllMap.getProperty(map.get(Conll.TOKEN_POSTAG) + map.get(Conll.TOKEN_FEATS)));
        }
        map.put(Conll.TOKEN_FULLTAG, fullTag);
        if (tokenArray.length > 7) {
        map.put(Conll.TOKEN_HEAD, tokenArray[Conll.TOKEN_HEAD]);
        map.put(Conll.TOKEN_DEPREL, tokenArray[Conll.TOKEN_DEPREL]);
        }
        if (tokenArray.length > 9) {
            map.put(Conll.TOKEN_PHEAD, tokenArray[Conll.TOKEN_PHEAD]);
            map.put(Conll.TOKEN_PDEPREL, tokenArray[Conll.TOKEN_PDEPREL]);
        }

        return map;

    }
    
}
