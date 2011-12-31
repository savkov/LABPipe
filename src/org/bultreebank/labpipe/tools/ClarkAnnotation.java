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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.bultreebank.labpipe.exceptions.MissingContentException;
import org.w3c.dom.Document;


import clark.common.ClarkRuntime;
import clark.internalFiles.DocInfo;
import clark.internalFiles.DocumentManager;
import clark.loader.DefaultFileFilter;
import clark.loader.Loader;
import clark.multiApply.ClarkProcessor;
import clark.multiApply.UniversalProcessor;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.bultreebank.labpipe.data.ClarkDocumentBuilder;
import org.bultreebank.labpipe.data.WebLicht;
import org.bultreebank.labpipe.exceptions.ClarkConfigurationException;
import org.bultreebank.labpipe.utils.Configuration;
import org.bultreebank.labpipe.utils.ServiceConstants;


/**
 *
 * @author Aleksandar Simov & Aleksandar Savkov
 */
public class ClarkAnnotation {

    private static String CONF_FILE;
    private static final Logger logger = Logger.getLogger(ClarkAnnotation.class.getName());
    private Configuration OPTIONS;
    ClarkProcessor processor = null;

    public ClarkAnnotation(Configuration options, String queryName) throws ClarkConfigurationException {

        OPTIONS = options;

        String clarkPath = null;
        String query = null;
        clarkPath = OPTIONS.getClarkHome();
        if (OPTIONS.containsKey(queryName)) {
            query = OPTIONS.getProperty(queryName);
        } else {
            throw new NullPointerException("No query name specified in 'conf.xml'");
        }

        if (clarkPath != null) {
            ClarkRuntime.initRuntime(clarkPath);
        } else {
            ClarkRuntime.initRuntime();
        }

        if (query != null) {
            try {
                this.processor = buildProcessor(query);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        } else {
            throw new ClarkConfigurationException("No query configuration provided");
        }
    }

    public ClarkAnnotation(String confFile, String queryName) throws ClarkConfigurationException {

        CONF_FILE = confFile;

        InputStream inProps = ClarkAnnotation.class.getClassLoader().getResourceAsStream(CONF_FILE);
        if (inProps == null) {
            inProps = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONF_FILE);
        }
        if (inProps == null) {
            try {
                inProps = new FileInputStream(CONF_FILE);
            } catch (IOException e) {
                logger.severe("Unable to load " + CONF_FILE);
                return;
            }
        }
        String clarkPath = null;
        String query = null;
        try {
            OPTIONS.load(inProps);
            clarkPath = OPTIONS.getClarkHome();
            if (OPTIONS.containsKey(queryName)) {
                query = OPTIONS.getProperty(queryName);
            } else {
                throw new NullPointerException();
            }

        } catch (IOException ex) {
            logger.severe(ex.getMessage());
            throw new RuntimeException("Error loading config file ", ex);
        } catch (NullPointerException ex) {
            logger.severe(ex.getMessage());
            throw new RuntimeException("Error loading config file ", ex);
        }
        if (clarkPath != null) {
            ClarkRuntime.initRuntime(clarkPath);
        } else {
            ClarkRuntime.initRuntime();
        }
        if (query != null) {
            try {
                this.processor = buildProcessor(query);
            } catch (Exception ex) {
                logger.severe(ex.getMessage());
                ex.printStackTrace();
            }
        } else {
            throw new ClarkConfigurationException("No query configuration provided");
        }
    }

    public String annotateTextData(String data) throws ClarkConfigurationException, MissingContentException {
        if (processor == null) {
            throw new ClarkConfigurationException("CLaRK processor is not initialized!");
        }
        if (data == null || data.length() == 0) {
            throw new MissingContentException("Empty string or no data provided for CLaRK annotation.");
        }

        Document inDoc = buildInputDoc(decode(data));
        processor.processDoc(inDoc, ServiceConstants.PIPE_CHARACTER_ENCODING, "laska.dtd");
        Document result = processor.getResult();
        return extractResultFragment(result);

    }

    /**
     * @deprecated current system date
     */
    public String annotateTextData(String data, int matherialID, int courceID,
            String language) {
        if (processor == null) {
            reportError(500, "CLaRK configuration failure");
        }
        if (data == null || data.length() == 0) {
            reportError(400, "No content provided");
        }
//        System.out.println("matherial_id=" + matherialID
//                + ", course_id=" + courceID
//                + ", lang=" + language
//                + ", content_length=" + ((data == null) ? 0 : data.length()));

        Document inDoc = buildInputDoc(decode(data));
        //System.out.println("Running processor ...");
        processor.processDoc(inDoc, "", null);
        Document result = processor.getResult();
        if (result == null) {
            reportError(500, "Internal error - no result doc generated");
        }
        //System.out.println("Extracting result");
        return extractResultFragment(result);

    }

    public String processXmlData(Document doc) {

        return processXmlData(doc, "weblicht.dtd");

    }

    public String processXmlData(Document doc, String dtd) {
        if (processor == null) {
            reportError(500, "CLaRK configuration failure");
        }
        if (doc == null) {
            reportError(400, "No content provided");
        }

//        System.out.println("Running processor ...");
        processor.processDoc(doc, "", dtd);
        Document result = processor.getResult();
        if (result == null) {
            reportError(500, "Internal error - no result doc generated");
        }
//        System.out.println("Extracting result");
        String resultString = extractResultFragment(doc);
        processor.clearData();
        return resultString;
    }

    public Document processXmlDocument(Document doc) throws ClarkConfigurationException, MissingContentException {
        return processXmlDocument(doc, "laska.dtd");
    }

    public Document processXmlDocument(Document doc, String dtd) throws ClarkConfigurationException, MissingContentException {
        if (processor == null) {
            throw new ClarkConfigurationException("CLaRK processor is not initialized!");
        }
        if (doc == null) {
            throw new MissingContentException("No data to process");
        }

//        System.out.println("Running processor ...");
        processor.processDoc(doc, "", dtd);
        Document result = processor.getResult();
        if (result == null) {
            reportError(500, "Internal error - no result doc generated");
        }
//        System.out.println("Extracting result");
        return result;
    }

    public void processWebLichtStream(InputStream is, OutputStream os) throws ParserConfigurationException, JAXBException, MissingContentException, ClarkConfigurationException {

        JAXBContext jc = JAXBContext.newInstance("de.dspin.data");
        Marshaller m = jc.createMarshaller();
        
        WebLicht doc = new WebLicht(is);

        processWebLicht(doc);

        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(doc, os);

    }

    public WebLicht processWebLicht(WebLicht doc) throws MissingContentException, ClarkConfigurationException {
        return processWebLicht(doc, null);
    }

    public WebLicht processWebLicht(WebLicht doc, HashMap escapes) throws MissingContentException, ClarkConfigurationException {

        String dtd = OPTIONS.getProperty("clarkDtd");

        Document tmpDoc = doc.toClark();
        Document result = processXmlDocument(tmpDoc, dtd);
        doc.importDSpin(new WebLicht(result));

        return doc;

    }

    public String decode(String in) {
        if (in == null) {
            return null;
        }
        try {
            return URLDecoder.decode(in, ServiceConstants.PIPE_CHARACTER_ENCODING);
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
            return "";
        }
    }

    public static void reportError(int statusCode, String message) {
//        Response error = Response.status(statusCode).build();
//        error.getMetadata().add("Error", message);
//        throw new WebApplicationException(error);
    }

    public void changeMultiQuery(String queryName) {

        String query = null;

        if (OPTIONS.containsKey(queryName)) {

            query = OPTIONS.getProperty(queryName);

        }

        if (query != null) {

            try {

                this.processor.clearData();
                this.processor = buildProcessor(query);

            } catch (Exception ex) {

                logger.severe(ex.getMessage());
                ex.printStackTrace();

            }

        } else {

            logger.severe("No query configuration provided");

        }

    }

    private ClarkProcessor buildProcessor(String queryName) throws Exception {

        DocumentManager manager = DocumentManager.getManager();
        Hashtable r = manager.getDocsInfo("Root");
        DocInfo infoQ = manager.getDocInfo("Root", queryName);
        if (infoQ == null) {
            throw new Exception("There is no such query(" + queryName + ") in the system!");
        }
        String fileName = infoQ.getFileName();
        Document queryDoc = Loader.loadDocumentAux(
                ClarkRuntime.getWorkPath() + "/data/" + fileName,
                null, DefaultFileFilter.UTF_16BE, false);
        if (queryDoc == null) {
            throw new Exception("Error loading query '" + queryName + "'");
        }
        return UniversalProcessor.constructProcessor(
                queryDoc, null, ClarkProcessor.CONTEXT_SOURCE);
    }

    private Document buildInputDoc(String content) {
        org.w3c.dom.Document doc = ClarkDocumentBuilder.buildClarkDocument();
        org.w3c.dom.Element root = doc.createElement("root");
        doc.appendChild(root);
        root.setTextContent(content);
        return doc;
    }

    private String extractResultFragment(Document doc) {
        try {
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, ServiceConstants.PIPE_CHARACTER_ENCODING);
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "0");

            StringWriter resultBuffer = new StringWriter();
            tr.transform(new DOMSource(doc), new StreamResult(resultBuffer));

            return resultBuffer.getBuffer().toString();
        } catch (Exception ex) {
            logger.severe(ex.getMessage());
        }
        return "";
    }
}
