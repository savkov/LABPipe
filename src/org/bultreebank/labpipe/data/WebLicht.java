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
package org.bultreebank.labpipe.data;

import de.dspin.data.DSpin;
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;
import org.bultreebank.labpipe.exceptions.MissingContentException;
import org.bultreebank.labpipe.utils.ClassMap;
import org.bultreebank.labpipe.utils.ServiceConstants;
import org.bultreebank.labpipe.utils.DataUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;

/**
 * <code>WebLicht</code> constitutes the a data unit encoded using the D-Spin 
 * WebLicht v.0.3 XML standard format. This class extends {@link DSpin} adding 
 * some functionality related to importing and exporting data.
 *
 * @author Aleksandar Savkov
 */
public class WebLicht extends DSpin {

    private static final Logger logger = Logger.getLogger(WebLicht.class.getName());

    /**
     * Creates an empty <code>WebLicht</code> object.
     * 
     */
    public WebLicht() {
    }

    /**
     * Creates a <code>WebLicht</code> object from a <code>DSpin</code> object.
     * 
     * @param   doc DSpin document
     */
    public WebLicht(DSpin doc) {
        importDSpin(doc);
    }

    /**
     * Unmarshalls a XML document read from the <code>InputStream</code> and 
     * creates an object based on it.
     * 
     * @param   is  <code>InputStream</code> containing a DSpin WebLicht XML document.
     * 
     */
    public WebLicht(InputStream is) {
        unmarshall(is);
    }

    /**
     * Unmarshalls a XML document from <code>dspin</code> and 
     * creates an object based on it.
     * 
     * @param   dspin   <code>String</code> containing a DSpin WebLicht XML document.
     */
    public WebLicht(String dspin) {
        unmarshall(dspin);
    }

    /**
     * Constructs A WebLicht object based on a CLaRK document.
     * 
     * @param   clarkDoc    CLaRK document
     */
    public WebLicht(Document clarkDoc) {

        ObjectFactory factory = new ObjectFactory();
        TextCorpus tc = factory.createTextCorpus();
        this.setTextCorpus(tc);

        List textCorpusList = tc.getTextOrTokensOrSentences();

        StringBuilder text = new StringBuilder();

        int index = 0;
        int sentIndex = 0;
        int tokenId = 1;
        int sentenceId = 1;
        int lemmaId = 1;
        int tagId = 1;

        List<Token> tokensXml;
        Tokens tokensList;
        tokensList = factory.createTokens();
        tokensXml = tokensList.getToken();


        List<Lemma> lemmasXml;
        Lemmas lemmasList;
        lemmasList = factory.createLemmas();
        lemmasXml = lemmasList.getLemma();

        List<Tag> tagsXml;
        POStags tagsList;
        tagsList = factory.createPOStags();
        tagsXml = tagsList.getTag();


        List<Sentence> sentencesXml;
        Sentences sentencesList;
        sentencesList = factory.createSentences();
        sentencesXml = sentencesList.getSentence();


        List<TokenRef> tokrefs = null;

        Sentence sentence = null;
        Lemma lemma = null;
        Tag tag = null;
        Token token = null;
        TokenRef tokref = null;

        String tokenTxt = null;
        String tagTxt = null;
        String lemmaTxt = null;
        String spacePunct = "[.,:;!?\"'(){}\\[\\]-]";

        NodeList sentences = clarkDoc.getElementsByTagName("s");
        NodeList tokens = null;

        for (int i = 0; i < sentences.getLength(); i++) {

            tokens = ((Element) sentences.item(i)).getElementsByTagName("tok");

            sentence = factory.createSentence();
            sentence.setID("sent".concat(String.valueOf(sentenceId)));
            sentence.setStart(index);
            tokrefs = sentence.getTokenRef();

            for (int j = 0; j < tokens.getLength(); j++) {

                tokenTxt = tokens.item(j).getTextContent();
                tagTxt = (((Element) tokens.item(j)).hasAttribute("ana")) ? ((Element) tokens.item(j)).getAttribute("ana") : ((Element) tokens.item(j)).getAttribute("svm");
                lemmaTxt = ((Element) tokens.item(j)).getAttribute("lm");


                if (!tokenTxt.matches(spacePunct)) {
                    index++;
                }

                token = factory.createToken();
                token.setID("tok".concat(String.valueOf(tokenId)));
                token.setValue(tokenTxt);
                token.setStart(index);
                token.setEnd(index + tokenTxt.length() - 1);
                tokensXml.add(token);

                tokref = factory.createTokenRef();
                tokref.setTokID(token);
                tokrefs.add(tokref);

                if (tagTxt != null && tagTxt.length() > 0) {
                    tag = factory.createTag();
                    tag.setID("tag".concat(String.valueOf(tagId)));
                    tag.setValue(tagTxt);
                    tag.setTokID(token);
                    tagsXml.add(tag);
                }

                if (lemmaTxt != null && lemmaTxt.length() > 0) {
                    lemma = factory.createLemma();
                    lemma.setID("lem".concat(String.valueOf(lemmaId)));
                    lemma.setTokID(token);
                    lemma.setValue(lemmaTxt);
                    lemmasXml.add(lemma);
                    lemmaId++;
                }

                if (!token.getValue().matches(ServiceConstants.PRE_SPACE_PUNCT_SIGNS_PATTERN)) {
                    text.append(" ");
                }
                text.append(token.getValue());

                index += tokenTxt.length() + 1;
                tokenId++;
                tagId++;
                sentIndex++;

            }

            sentence.setEnd(index - 1);
            sentencesXml.add(sentence);
            sentenceId++;

        }

        textCorpusList.add(text.toString());
        textCorpusList.add(tokensList);
        if (tagsXml.size() > 0) {
            textCorpusList.add(tagsList);
        }
        if (lemmasXml.size() > 0) {
            textCorpusList.add(lemmasList);
        }
        textCorpusList.add(sentencesList);


    }

    /**
     * Imports a DSpin object data into this object.
     * 
     * @param   doc DSpin object
     */
    public final void importDSpin(DSpin doc) {
        this.lexicon = doc.getLexicon();
        this.metaData = doc.getMetaData();
        this.textCorpus = doc.getTextCorpus();
        this.version = doc.getVersion();
    }

    /**
     * Unmarshalls <code>InputStream</code> into a <code>DSpin</code> object and
     * then imports its content into this object.
     * @param is 
     */
    public final void unmarshall(InputStream is) {
        try {
            JAXBContext context = JAXBContext.newInstance("de.dspin.data");
            Unmarshaller u = context.createUnmarshaller();
            importDSpin((DSpin) u.unmarshal(is));

        } catch (JAXBException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_JAXB, ex);
        }

    }

    /**
     * Unmarshalls a <code>String</code> into a <code>DSpin</code> object and
     * then imports its content into this object.
     * @param dspin 
     */
    public final void unmarshall(String dspin) {

        try {
            JAXBContext context = JAXBContext.newInstance("de.dspin.data");
            Unmarshaller u = context.createUnmarshaller();
            importDSpin((DSpin) u.unmarshal(new ByteArrayInputStream(dspin.getBytes(ServiceConstants.PIPE_CHARACTER_ENCODING))));
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_UNSUPPORTED_ENCODING, ex);
        } catch (JAXBException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_JAXB, ex);
        }

    }
    
    /**
     * Converts this document's XML representation into <code>String</code>.
     * 
     * @return  String - this document's XML representation
     */
    @Override
    public String toString() {

        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        this.exportAsXML(baos);
        try {
            return baos.toString(ServiceConstants.PIPE_CHARACTER_ENCODING);
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_UNSUPPORTED_ENCODING, ex);
            return null;
        }

    }

    /**
     * Prints this object's text representation (the <code>text</code> element
     * from the underlying <code>DSpin</code> document) into the <code>OutputStream</code>.
     * 
     * @param   os  <code>OutputStream</code> to receive the text part of this document.
     */
    public void toText(OutputStream os) {
        try {
            TextCorpus tc = this.getTextCorpus();
            ClassMap cm = new ClassMap(tc.getTextOrTokensOrSentences());
            if (!cm.containsKey(String.class)) {
                return;
            }
            String text = cm.get(String.class).toString();
            String buff = "";

            while (text.length() > 0) {
                if (text.length() > 1000) {
                    buff = text.substring(0, 1000);
                    text = text.substring(1000);
                } else {
                    buff = text;
                    text = "";
                }
                os.write(buff.getBytes());

            }

            os.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_IO, ex);
        }

    }

    /**
     * Exports this object as a DOM <code>Node</code>.
     * 
     * @param   out DOM <code>Node</code> object receiving the data
     */
    public void exportAsXML(Node out) {
        try {
            JAXBContext jc = JAXBContext.newInstance("de.dspin.data");
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(this, out);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Problem with exporting to XML.", ex);
        }
    }
    
   /**
     * Writes this object into a <code>XMLStreamWriter</code>.
     * 
     * @param   out DOM <code>XMLStreamWriter</code> object receiving the data
     */
    public void exportAsXML(XMLStreamWriter out) {
        try {
            JAXBContext jc = JAXBContext.newInstance("de.dspin.data");
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(this, out);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Problem with exporting to XML.", ex);
        }
    }

    /**
     * Writes this object into a <code>XMLEventWriter</code>.
     * 
     * @param   out <code>XMLEventWriter</code> object receiving the data
     */
    public void exportAsXML(XMLEventWriter out) {
        try {
            JAXBContext jc = JAXBContext.newInstance("de.dspin.data");
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(this, out);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Problem with exporting to XML.", ex);
        }
    }

    /**
     * Relays this object to a <code>ContentHandler</code>.
     * 
     * @param   out <code>ContentHandler</code> object receiving the data
     */
    public void exportAsXML(ContentHandler out) {
        try {
            JAXBContext jc = JAXBContext.newInstance("de.dspin.data");
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(this, out);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Problem with exporting to XML.", ex);
        }
    }

    /**
     * Writes this object into a <code>File</code>.
     * 
     * @param   out <code>File</code> object receiving the data
     */
    public void exportAsXML(File out) {
        try {
            JAXBContext jc = JAXBContext.newInstance("de.dspin.data");
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(this, out);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Problem with exporting to XML.", ex);
        }
    }

    /**
     * Writes this object into a <code>Writer</code>.
     * 
     * @param   out <code>Writer</code> object receiving the data
     */
    public void exportAsXML(Writer out) {
        try {
            JAXBContext jc = JAXBContext.newInstance("de.dspin.data");
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(this, out);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Problem with exporting to XML.", ex);
        }
    }

    /**
     * Writes this object into a <code>OutputStream</code>.
     * 
     * @param   out <code>OutputStream</code> object receiving the data
     */
    public void exportAsXML(OutputStream out) {
        try {
            JAXBContext jc = JAXBContext.newInstance("de.dspin.data");
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(this, out);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Problem with exporting to XML.", ex);
        }
    }

    /**
     * Converts this object to a {@link Conll} object.
     * 
     * @param   conllMap    <code>Map</code> of POS tag forms linking BTB 
     *                      original forms to the CoNLL appropriate ones.
     * @return  {@link Conll}
     * @throws MissingContentException  
     * 
     */
    public Conll toConll(Properties conllMap) throws MissingContentException {

        ClassMap cm = new ClassMap(this.getTextCorpus().getTextOrTokensOrSentences());

        if (cm.get(Tokens.class) == null || cm.get(POStags.class) == null || cm.get(Sentences.class) == null) {
            return null;
        }


        List<Sentence> sentenceList = ((Sentences) cm.get(Sentences.class)).getSentence();

        Conll conllArray = new Conll();

        for (Sentence sentence : sentenceList) {

            conllArray.add(DataUtils.dspinSentAsConllArray(sentence, cm, conllMap));

        }

        return conllArray;
    }

    /**
     * Exports this objects <code>text</code> element into a XML document 
     * containing only text.
     * 
     * @return Document - CLaRK Text document
     */
    public Document toTextDataDocument() {

        Document textDoc = ClarkDocumentBuilder.buildClarkDocument();

        List tcList = this.getTextCorpus().getTextOrTokensOrSentences();
        ClassMap cm = new ClassMap(tcList);

        if (cm.get(String.class) == null) {
            throw new NullPointerException("There is no text element.");
        }
        String text = cm.get(String.class).toString();
        Element textdata = textDoc.createElement("textdata");
        textdata.setTextContent(text);
        textDoc.appendChild(textdata);
        
        return textDoc;

    }

    /**
     * Converts this object to CLaRK data document
     * 
     * @return Document - CLaRK data document
     * @throws MissingContentException  
     */
    public Document toClark() throws MissingContentException {
        return toClark(null);
    }

    /**
     * Converts this object to CLaRK data document
     * 
     * @param   escapes a <code>Map</code> containing links between 
     *                  <code>Token</code> elements and escaped sequences 
     *                  (This is not yet implement in the new setting of LABPipe)
     * 
     * @return Document - CLaRK data document
     * @throws MissingContentException  
     */
    public Document toClark(HashMap<Token, List<String>> escapes) throws MissingContentException {

        Document clarkDoc = ClarkDocumentBuilder.buildClarkDocument();

        if (escapes == null) {
            escapes = new HashMap<Token, List<String>>();
        }
        List tcList = this.getTextCorpus().getTextOrTokensOrSentences();
        ClassMap cm = new ClassMap(tcList);
        Element root = clarkDoc.createElement("root");
        clarkDoc.appendChild(root);

        if (cm.get(Tokens.class) == null || cm.get(Sentences.class) == null) {
            throw new MissingContentException();
        }

        List<Token> tokenListXml = ((Tokens) cm.get(Tokens.class)).getToken();
        List<Tag> tagListXml = (cm.containsKey(POStags.class)) ? ((POStags) cm.get(POStags.class)).getTag() : null;
        List<Lemma> lemmaListXml = (cm.containsKey(Lemmas.class)) ? ((Lemmas) cm.get(Lemmas.class)).getLemma() : null;
        List<Sentence> sentenceListXml = ((Sentences) cm.get(Sentences.class)).getSentence();

        int tokenIndex = 0;

        for (Sentence sentence : sentenceListXml) {

            Element s = clarkDoc.createElement("s");
            s.setAttribute("id", sentence.getID());

            int sentenceSize = sentence.getTokenRef().size();

            for (int i = 0; (i < sentenceSize) && (tokenIndex < tokenListXml.size()); i++, tokenIndex++) {

                Element t = clarkDoc.createElement("tok");

                Token token = tokenListXml.get(tokenIndex);
                t.setTextContent(token.getValue());

                if (tagListXml != null) {
                    Tag tag = tagListXml.get(tokenIndex);
                    t.setAttribute("svm", tag.getValue());
                }

                if (lemmaListXml != null) {
                    Lemma lemma = (Lemma) DataUtils.getBackRef(lemmaListXml, token);
                    if (lemma != null) {
                        t.setAttribute("lm", lemma.getValue());
                    }
                }

                s.appendChild(t);

                if (escapes.containsKey(token)) {
                    for (String esc : escapes.get(token)) {
                        t = clarkDoc.createElement("tok");
                        t.setTextContent(esc);
                        t.setAttribute("esc", "true");
                    }
                }

            }

            root.appendChild(s);

        }

        return clarkDoc;

    }

    /**
     * Converts this object into a Line encoded <code>String</code> 
     * representation and prints it into the <code>OutputStream</code>.
     * 
     * @param   os  <code>OutputStream</code> receiving the Line encoded data
     * @param   eosToken    end of sentence token
     * @param   gaze    <code>boolean</code> flag indicating Gaze representation
     * @throws MissingContentException  
     * 
     */
    public void toLines(OutputStream os, String eosToken, boolean gaze) throws MissingContentException {


        ClassMap cm = new ClassMap(this.getTextCorpus().getTextOrTokensOrSentences());

        POStags tags = null;
        List<Tag> tagList = null;
        Lemmas lemmas = null;
        List<Lemma> lemmaList = null;

        if (!cm.containsKey(Tokens.class)) {
            throw new MissingContentException("Missing Tokens element in the input file during WebLichtSentences2Line conversion.");
        } else if (!cm.containsKey(Sentences.class)) {
            throw new MissingContentException("Missing Sentences element in the input file during WebLichtSentences2Line conversion.");
        }

        if (cm.containsKey(POStags.class)) {
            tags = (POStags) cm.get(POStags.class);
            tagList = tags.getTag();
        }
        if (cm.containsKey(Lemmas.class)) {
            lemmas = (Lemmas) cm.get(Lemmas.class);
            lemmaList = lemmas.getLemma();
        }

        Sentences sentences = (Sentences) cm.get(Sentences.class);
        List<Sentence> sentenceList = sentences.getSentence();

        OutputStreamWriter osw = new OutputStreamWriter(os);
        try {

            for (Sentence s : sentenceList) {

                List<TokenRef> tokenRefList = s.getTokenRef();

                for (TokenRef t : tokenRefList) {
                    Token token = (Token) t.getTokID();
                    osw.write(token.getValue());
                    Tag tag = (Tag) DataUtils.getBackRef(tagList, token);
                    Lemma lemma = (Lemma) DataUtils.getBackRef(lemmaList, token);
                    if (tag != null) {
                        osw.write("\t");
                        osw.write(tag.getValue());
                    }
                    if (lemma != null && !gaze) {
                        osw.write("\t");
                        osw.write(lemma.getValue());
                    }
                    if (tag != null && gaze) {
                        osw.write("\t");
                        osw.write("O");
                    }
                    osw.write("\n");
                }

                if (gaze) {
                    osw.write(ServiceConstants.GAZE_EOS_TOKEN);
                } else {
                    osw.write(eosToken);
                }
                osw.write("\n");

            }

        } catch (IOException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_IO, ex);
        } finally {
            try {
                osw.close();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_IO, ex);
            }
        }
    }
}
