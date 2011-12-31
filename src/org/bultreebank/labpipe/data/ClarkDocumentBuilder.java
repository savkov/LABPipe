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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.bultreebank.labpipe.exceptions.MissingContentException;
import org.bultreebank.labpipe.utils.ServiceConstants;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Aleksandar Savkov
 */
public class ClarkDocumentBuilder {

    private static final Logger logger = Logger.getLogger(ClarkDocumentBuilder.class.getName());
    private static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    private static DocumentBuilder db;

    public static Document buildClarkDocument() {
        iniDocFactory();
        return db.newDocument();
    }

    public static Document buildClarkDocument(String doc) throws MissingContentException {
        
        if (doc == null || doc.equals("")) {
            throw new MissingContentException();
        }
        
        try {
            iniDocFactory();
            return db.parse(new ByteArrayInputStream(doc.getBytes(ServiceConstants.PIPE_CHARACTER_ENCODING)));
        } catch (SAXException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_SAX_PARSER, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_IO, ex);
        }
        
        return null;
        
    }

    public static Document buildClarkDocument(InputStream is) {
        try {
            iniDocFactory();
            return db.parse(is);
        } catch (SAXException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_SAX_PARSER, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_IO, ex);
        }
        
        return null;
        
    }

    private static void iniDocFactory() {
        try {
            dbf.setValidating(false);
            dbf.setFeature("http://xml.org/sax/features/namespaces", false);
            dbf.setFeature("http://xml.org/sax/features/validation", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_PARSER_CONFIGURATION, ex);
        }
    }

    
    
}
