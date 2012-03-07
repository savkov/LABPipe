/*
 * LABPipe - Natural Language Processing Pipeline for Bulgarian
 * Copyright (C) 2011 Institute for Information and Communication Technologies 
 
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
package org.bultreebank.labpipe.data;

import de.dspin.data.textcorpus.Dependency;
import de.dspin.data.textcorpus.Depparsing;
import de.dspin.data.textcorpus.Depparsing.Parse;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bultreebank.labpipe.utils.DataUtils;
import org.bultreebank.labpipe.utils.ServiceConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <code>Conll</code> represents the CoNLL data format used in LABPipe. It extends 
 * ArrayList&lt;ArrayList&lt;String&gt;&gt; placing each sentence in a separate 
 * ArrayList&lt;String&gt; object each token (and its properties) being an item 
 * in it.
 *
 * @author Aleksandar Savkov
 */
public class Conll extends ArrayList<ArrayList<String>> {

    private static final Logger logger = Logger.getLogger(Conll.class.getName());
    /**
     * Token index used in <code>Map</code> objects produced by {@link DataUtils#conllLineAsMap}
     */
    public static final int TOKEN_INDEX = 0;
    /**
     * Token form index used in <code>Map</code> objects produced by {@link DataUtils#conllLineAsMap}
     */
    public static final int TOKEN_FORM = 1;
    /**
     * Token lemma index used in <code>Map</code> objects produced by {@link DataUtils#conllLineAsMap}
     */
    public static final int TOKEN_LEMMA = 2;
    /**
     * Token CPOS tag (long tag; first two characters of the full BTB tag) index used 
     * in <code>Map</code> objects produced by {@link DataUtils#conllLineAsMap}
     */
    public static final int TOKEN_CPOSTAG = 3;
    /**
     * Token short POS tag (usually the first character of the full BTB tag) index
     * used in <code>Map</code> objects produced by {@link DataUtils#conllLineAsMap}
     */
    public static final int TOKEN_POSTAG = 4;
    /**
     * Token POS tag features index used in <code>Map</code> objects produced by 
     * {@link DataUtils#conllLineAsMap}
     */
    public static final int TOKEN_FEATS = 5;
    /**
     * Token dependency head index used in <code>Map</code> objects produced by {@link DataUtils#conllLineAsMap}
     */
    public static final int TOKEN_HEAD = 6;
    /**
     * Token dependency relation index used in <code>Map</code> objects produced by {@link DataUtils#conllLineAsMap}
     */
    public static final int TOKEN_DEPREL = 7;
    /**
     * Token projectile dependency head index used in <code>Map</code> objects produced by {@link DataUtils#conllLineAsMap}
     */
    public static final int TOKEN_PHEAD = 8;
    /**
     * Token projectile dependency relation index used in <code>Map</code> objects produced by {@link DataUtils#conllLineAsMap}
     */
    public static final int TOKEN_PDEPREL = 9;
    /**
     * Token BTB full-tag index used in <code>Map</code> objects produced by {@link DataUtils#conllLineAsMap}
     */
    public static final int TOKEN_FULLTAG = 10;

    /**
     * Creates empty <code>Conll</code> object.
     */
    public Conll() {
    }

    /**
     * Creates <code>Conll</code> object from a CoNLL data format <code>String</code>.
     * 
     * @param   conll   CoNLL data formated <code>String</code>.
     * 
     */
    public Conll(String conll) {
        try {
            this.loadConll(new ByteArrayInputStream(conll.getBytes(ServiceConstants.PIPE_CHARACTER_ENCODING)));
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Parses the CoNLL encoded data from the <code>InputStream</code> into a 
     * <code>Conll</code> object
     * 
     * @param   is  CoNLL encoded data <code>InputStream</code>
     * 
     */
    public Conll(InputStream is) {

        this.loadConll(is);

    }

    /*
     * Loads Conll from an input stream.
     */
    private void loadConll(InputStream is) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, ServiceConstants.PIPE_CHARACTER_ENCODING));
            ArrayList<String> sentence = new ArrayList(100);

            String line;
            int id = 1;

            while ((line = br.readLine()) != null) {

                if (!line.matches("^[0-9]+.+")) {
                    this.add(sentence);
                    sentence = new ArrayList(100);
                    id = 1;
                } else {
                    sentence.add(line);
                    id++;
                }

            }

            sentence.trimToSize();

            if (sentence.size() > 0) {
                this.add(sentence);
            }

        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Converts this object into a <code>String</code> using the CoNLL data encoding format.
     * 
     * @return String
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ArrayList<String> s : this) {
            for (String l : s) {
                sb.append(l);
                sb.append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Converts this object to a Line encoded data <code>String</code> and prints it into the <code>OutputStream</code>.
     * 
     * @param   os  data <code>OutputStream</code>
     * @param   eosToken    end of sentence token
     * @param   iConllMap   <code>Map</code> containing back connections between
     *                      POS tags in CoNLL representation and their original 
     *                      BTB forms.
     */
    public void toLine(OutputStream os, String eosToken, Properties iConllMap) {

        try {

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
            bw.write(this.toLine(eosToken, iConllMap));
            bw.close();

        } catch (IOException ex) {
            logger.severe(ServiceConstants.EXCEPTION_IO);
        }

    }

    /**
     * Converts this object to a Line encoded data <code>String</code>.
     * 
     * @param   eosToken    end of sentence token
     * @param   iConllMap   <code>Map</code> containing back connections between
     *                      POS tags in CoNLL representation and their original 
     *                      BTB forms.
     * @return  String
     */
    public String toLine(String eosToken, Properties iConllMap) {

        StringBuilder lines = new StringBuilder();
        HashMap map;

        for (ArrayList<String> sentence : this) {

            for (String line : sentence) {

                map = DataUtils.conllLineAsMap(line, iConllMap);
                lines.append(map.get(Conll.TOKEN_FORM));
                lines.append("\t");
                lines.append(map.get(Conll.TOKEN_FULLTAG));
                lines.append("\n");

            }

            lines.append(eosToken);
            lines.append("\n");

        }

        return lines.toString();

    }

    /**
     * Converts this object into CLaRK document and prints it into the <code>OutputStream</code>.
     * 
     * @param   os  CLaRK document <code>OutputStream</code>
     * @param   iConllMap   <code>Map</code> containing back connections between
     *                      POS tags in CoNLL representation and their original 
     *                      BTB forms.
     */
    public void toClark(OutputStream os, Properties iConllMap) {

        try {
            StringBuilder doc = new StringBuilder();

            doc.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            doc.append("<root>\n");
            
            HashMap<Integer,String> map;
            
            String word;
            String lemma;
            String svm;

            for (ArrayList<String> sentence : this) {
                doc.append("\t<s>\n");
                for (String line : sentence) {
                    map = DataUtils.conllLineAsMap(line, iConllMap);

                    word = map.get(Conll.TOKEN_FORM);
                    lemma = map.get(Conll.TOKEN_LEMMA);
                    svm = map.get(Conll.TOKEN_FULLTAG);
                    
                    doc.append("\t\t<tok");
                    if (lemma != null && !lemma.equals("_")) {
                        doc.append(" lm=\"");
                        doc.append(lemma);
                        doc.append("\"");
                    }
                    if (svm != null && !svm.equals("_")) {
                        doc.append(" svm=\"");
                        doc.append(svm);
                        doc.append("\"");
                    }
                    doc.append(">");
                    doc.append(word);
                    doc.append("</tok>\n");
                }
                doc.append("\t</s>\n");
            }

            doc.append("</root>");

            os.write(doc.toString().getBytes(ServiceConstants.PIPE_CHARACTER_ENCODING));
            os.close();
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_UNSUPPORTED_ENCODING, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_IO, ex);
        }

    }

    /**
     * Converts this object into CLaRK document.
     * 
     * @param   iConllMap   <code>Map</code> containing back connections between
     *                      POS tags in CoNLL representation and their original 
     *                      BTB forms.
     * @return Document - CLaRK document
     */
    public Document toClark(Properties iConllMap) {

        Document clarkDoc = ClarkDocumentBuilder.buildClarkDocument();

        Element root = clarkDoc.createElement("root");
        clarkDoc.appendChild(root);
        Element sentence;
        Element token;

        for (ArrayList<String> s : this) {
            sentence = clarkDoc.createElement("s");
            root.appendChild(sentence);
            for (String l : s) {

                HashMap<Integer, String> conllLineMap = DataUtils.conllLineAsMap(l, iConllMap);
                token = clarkDoc.createElement("tok");
                sentence.appendChild(token);
                token.setTextContent(conllLineMap.get(Conll.TOKEN_FORM));
                if (!conllLineMap.get(Conll.TOKEN_FULLTAG).equals("_")) {
                    token.setAttribute("svm", conllLineMap.get(Conll.TOKEN_FULLTAG));
                }
                if (conllLineMap.containsKey(Conll.TOKEN_LEMMA) && !conllLineMap.get(Conll.TOKEN_LEMMA).equals("_")) {
                    token.setAttribute("lm", conllLineMap.get(Conll.TOKEN_LEMMA));
                }

            }
        }

        return clarkDoc;

    }

    /**
     * Converts this object into {@link WebLicht} document
     * 
     * @param   iConllMap   <code>Map</code> containing back connections between
     *                      POS tags in CoNLL representation and their original 
     *                      BTB forms.
     * 
     * @return {@link WebLicht}
     * 
     */
    public WebLicht toWebLicht(Properties iConllMap) {
        return toWebLicht(this, iConllMap);
    }

    /**
     * Converts the <code>Conll</code> object into a {@link WebLicht} document.
     * 
     * @param   conll   {@link Conll} object
     * @param   iConllMap   <code>Map</code> containing back connections between
     *                      POS tags in CoNLL representation and their original 
     *                      BTB forms.
     * 
     * @return  {@link WebLicht}
     */
    public static WebLicht toWebLicht(Conll conll, Properties iConllMap) {

        WebLicht doc = new WebLicht();

        ObjectFactory factory = new ObjectFactory();
        TextCorpus tc = factory.createTextCorpus();
        doc.setTextCorpus(tc);
        List tcList = tc.getTextOrTokensOrSentences();
        tcList.clear();

        StringBuilder text = new StringBuilder();
        Tokens tokens = factory.createTokens();
        POStags tags = factory.createPOStags();
        Lemmas lemmas = factory.createLemmas();
        Sentences sentences = factory.createSentences();
        Depparsing depparsing = factory.createDepparsing();

        List<Token> tokenList = tokens.getToken();
        List<Tag> tagList = tags.getTag();
        List<Lemma> lemmaList = lemmas.getLemma();
        List<Sentence> sentenceList = sentences.getSentence();
        List<Parse> parseList = depparsing.getParse();
        Depparsing.Parse parse;
        List<Dependency> dependencyList;
        List<TokenRef> trList;

        int charId = 1;
        int tokId = 1;
        int tagId = 1;
        int lemId = 1;
        int sentId = 1;
        int parId = 1;

        for (ArrayList<String> sentenceArray : conll) {

            Sentence sentence = factory.createSentence();
            sentence.setID(String.valueOf(sentId));
            sentId++;
            sentence.setStart(tokId);
            sentence.setEnd(tokId + sentenceArray.size() - 1);
            sentenceList.add(sentence);
            trList = sentence.getTokenRef();
            parse = factory.createDepparsingParse();
            parse.setID("par" + parId);
            parId++;
            parseList.add(parse);
            dependencyList = parse.getDependency();

            Token[] tokenRefs = new Token[sentenceArray.size() + 1];

            tokenRefs[0] = null;

            for (int i = 1; i <= sentenceArray.size(); i++) {

                tokenRefs[i] = factory.createToken();

            }

            for (String tokenArrayStr : sentenceArray) {

                HashMap<Integer, String> conllLine = DataUtils.conllLineAsMap(tokenArrayStr, iConllMap);

                String tokenStr = conllLine.get(Conll.TOKEN_FORM);
                String tagStr = conllLine.get(Conll.TOKEN_FULLTAG);
                String lemmaStr = conllLine.get(Conll.TOKEN_LEMMA);

                if (text.length() > 0
                        && (!tokenStr.matches(ServiceConstants.PRE_SPACE_PUNCT_SIGNS_PATTERN)
                        || Character.toString(text.charAt(text.length() - 1)).matches(ServiceConstants.POST_SPACE_PUNCT_SIGNS_PATTERN))) {
                    text.append(" ");
                }

                text.append(tokenStr);

                Token token = tokenRefs[Integer.valueOf(conllLine.get(Conll.TOKEN_INDEX))];
                token.setValue(tokenStr);
                token.setStart(charId);
                token.setID("tok" + tokId);
                charId += tokenStr.length();
                token.setEnd(charId - 1);
                tokenList.add(token);
                tokId++;

                TokenRef tr = factory.createTokenRef();
                tr.setTokID(token);
                trList.add(tr);

                if (!lemmaStr.equals("_")) {
                    Lemma lemma = factory.createLemma();
                    lemma.setID("lem" + lemId);
                    lemma.setTokID(token);
                    lemma.setValue(conllLine.get(Conll.TOKEN_LEMMA));
                    lemmaList.add(lemma);
                    lemId++;
                }

                if (tagStr == null) {
                    System.out.println(tokenArrayStr);
                }

                if (!tagStr.equals("_")) {
                    Tag tag = factory.createTag();
                    tag.setValue(tagStr);
                    tag.setTokID(token);
                    tag.setID("tag" + tagId);
                    tagList.add(tag);
                    tagId++;
                }

                if (conllLine.containsKey(Conll.TOKEN_DEPREL)
                        && !conllLine.get(Conll.TOKEN_DEPREL).equals("_")) {
                    Dependency dependency = factory.createDependency();
                    dependency.setDepID(token);
                    dependency.setGovID(tokenRefs[Integer.valueOf(conllLine.get(Conll.TOKEN_HEAD))]);
                    dependency.setFunc(conllLine.get(Conll.TOKEN_DEPREL));
                    dependencyList.add(dependency);
                }

            }

        }

        tcList.add(text.toString());
        tcList.add(tokens);
        tcList.add(lemmas);
        tcList.add(tags);
        tcList.add(sentences);
        tcList.add(depparsing);

        return doc;

    }
}