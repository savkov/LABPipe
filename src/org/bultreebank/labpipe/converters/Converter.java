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
package org.bultreebank.labpipe.converters;

import java.util.logging.Level;
import org.bultreebank.labpipe.data.Conll;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Logger;
import org.bultreebank.labpipe.data.ClarkDocumentBuilder;
import org.bultreebank.labpipe.data.WebLicht;
import org.bultreebank.labpipe.exceptions.IncorrectInputException;
import org.bultreebank.labpipe.exceptions.IncorrectOutputException;
import org.bultreebank.labpipe.exceptions.MissingContentException;
import org.bultreebank.labpipe.utils.Configuration;
import org.bultreebank.labpipe.utils.Misc;
import org.bultreebank.labpipe.utils.ServiceConstants;
import org.bultreebank.labpipe.utils.XmlUtils;
import org.w3c.dom.Document;

/**
 * <code>Converter</code> is a higher level class managing the conversions 
 * between all data formats used in <code>LABPipe</code>.
 *
 * @author Aleksandar Savkov
 */
public class Converter {

    private static final Logger logger = Logger.getLogger(Converter.class.getName());
    private Configuration OPTIONS;
    private String EOS_TOKEN;
    private Properties CONLL_MAP = new Properties();
    private Properties CONLL_MAP_INV = new Properties();

    /**
     * Creates a new object based on the provided configuration.
     * 
     * @param   options <code>LABPipe</code> configuration
     * @throws IOException  
     */
    public Converter(Configuration options) throws IOException {
        OPTIONS = options;
        EOS_TOKEN = OPTIONS.getProperty(Configuration.EOS_TOKEN);
        CONLL_MAP.load(new FileInputStream(OPTIONS.getConllMapPath()));
        CONLL_MAP_INV.load(new FileInputStream(OPTIONS.getConllMapInvPath()));
    }

    /**
     * Converts data read from the <code>InputStream</code> into another data 
     * type and writes it into the <code>OutputStream</code>.
     * 
     * @param   is  input data <code>InputStream</code>
     * @param   os  converted data <code>OutputStream</code>
     * @param   inputType   input data type
     * @param   outputType  output data type
     * @throws MissingContentException 
     * @throws IncorrectInputException
     * @throws IncorrectOutputException  
     * 
     */
    public void convert(InputStream is, OutputStream os, int inputType, int outputType) throws MissingContentException, IncorrectInputException, IncorrectOutputException {

        if (inputType == outputType) {
            Misc.writeStream(is, os);
        }

        if (inputType == ServiceConstants.DATA_LINE) {

            if (outputType == ServiceConstants.DATA_GAZE) {
                Converter.line2gaze(is, os, EOS_TOKEN);
            } else if (outputType == ServiceConstants.DATA_CLARK_TOKENS || outputType == ServiceConstants.DATA_CLARK_TAGS) {
                Converter.line2clark(is, os, EOS_TOKEN);
            } else if (outputType == ServiceConstants.DATA_CONLL) {
                Converter.line2conll(is, os, EOS_TOKEN, CONLL_MAP);
            } else if (outputType == ServiceConstants.DATA_WEBLICHT) {
                LineConverter.toWebLicht(is, EOS_TOKEN).exportAsXML(os);
            } else {
                throw new IncorrectOutputException("Converter unable to convert LINE into assigned output data format: ".concat(String.valueOf(outputType)));
            }

        } else if (inputType == ServiceConstants.DATA_CLARK_TOKENS || inputType == ServiceConstants.DATA_CLARK_TAGS) {

            if (outputType == ServiceConstants.DATA_LINE) {
                Converter.clark2line(is, os, EOS_TOKEN);
            } else if (outputType == ServiceConstants.DATA_GAZE && inputType == ServiceConstants.DATA_CLARK_TAGS) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
                Converter.clark2line(is, baos, EOS_TOKEN);
                Converter.line2gaze(new ByteArrayInputStream(baos.toByteArray()), os, EOS_TOKEN);
            } else if (outputType == ServiceConstants.DATA_CONLL) {
                Converter.clark2conll(is, os, CONLL_MAP);
            } else if (outputType == ServiceConstants.DATA_WEBLICHT) {
                WebLicht doc = new WebLicht(ClarkDocumentBuilder.buildClarkDocument(is));
                doc.exportAsXML(os);
            } else {
                throw new IncorrectOutputException("Converter unable to convert CLARK_TOKENS/CLARK_TAGS into assigned output data format: ".concat(String.valueOf(outputType)));
            }

        } else if (inputType == ServiceConstants.DATA_CONLL) {// This does not work yet. Needs a bidirectional tag-features map.

            if (outputType == ServiceConstants.DATA_LINE) {
                Converter.conll2line(is, os, EOS_TOKEN, CONLL_MAP_INV);
            } else if (outputType == ServiceConstants.DATA_GAZE) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
                Converter.conll2line(is, baos, EOS_TOKEN, CONLL_MAP_INV);
                Converter.line2gaze(new ByteArrayInputStream(baos.toByteArray()), os, EOS_TOKEN);
            } else if (outputType == ServiceConstants.DATA_CLARK_TOKENS || outputType == ServiceConstants.DATA_CLARK_TAGS) {
                Converter.conll2clark(is, os, CONLL_MAP_INV);
            } else if (outputType == ServiceConstants.DATA_WEBLICHT) {
                Conll conll = new Conll(is);
                conll.toWebLicht(conll, CONLL_MAP_INV).exportAsXML(os);
            } else {
                throw new IncorrectOutputException("Converter unable to convert CONLL into assigned output data format: ".concat(String.valueOf(outputType)));
            }

        } else if (inputType == ServiceConstants.DATA_WEBLICHT) {

            WebLicht doc = new WebLicht(is);
            if (outputType == ServiceConstants.DATA_LINE) {
                doc.toLines(os, EOS_TOKEN, false);
            } else if (outputType == ServiceConstants.DATA_GAZE) {
                doc.toLines(os, EOS_TOKEN, true);
            } else if (outputType == ServiceConstants.DATA_CLARK_TOKENS || outputType == ServiceConstants.DATA_CLARK_TAGS) {
                XmlUtils.print(doc.toClark(), os);
            } else if (outputType == ServiceConstants.DATA_CONLL) {
                try {
                    os.write(doc.toConll(CONLL_MAP).toString().getBytes(ServiceConstants.PIPE_CHARACTER_ENCODING));
                } catch (UnsupportedEncodingException ex) {
                    logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_UNSUPPORTED_ENCODING, ex);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_IO, ex);
                }
            } else {
                throw new IncorrectOutputException("Converter unable to convert WEBLICHT into assigned output data format: ".concat(String.valueOf(outputType)));
            }

        } else {
            throw new IncorrectInputException("Converter unable to convert input data: ".concat(String.valueOf(inputType)));
        }

    }

    
    /**
     * Converts input data object into an object of another data type.
     * 
     * @param   input  input data object
     * @param   inputType   input data type
     * @param   outputType  output data type
     * 
     * @return  {@link Object} of the <code>outputType</code>
     * @throws MissingContentException
     * @throws IncorrectInputException
     * @throws IncorrectOutputException  
     */
    public Object convert(Object input, int inputType, int outputType) throws MissingContentException, IncorrectInputException, IncorrectOutputException {

        if (inputType == outputType) {
            return input;
        }

        if (inputType == ServiceConstants.DATA_LINE) {

            if (outputType == ServiceConstants.DATA_GAZE) {
                return Converter.line2gaze((String) input, EOS_TOKEN);
            } else if (outputType == ServiceConstants.DATA_CLARK_TOKENS || outputType == ServiceConstants.DATA_CLARK_TAGS) {
                return Converter.line2clark((String) input, EOS_TOKEN);
            } else if (outputType == ServiceConstants.DATA_CONLL) {
                return Converter.line2conll((String) input, EOS_TOKEN, CONLL_MAP);
            } else if (outputType == ServiceConstants.DATA_WEBLICHT) {
                try {
                    return LineConverter.toWebLicht(new ByteArrayInputStream(((String)input).getBytes(ServiceConstants.PIPE_CHARACTER_ENCODING)), EOS_TOKEN);
                } catch (UnsupportedEncodingException ex) {
                    logger.severe(ServiceConstants.EXCEPTION_UNSUPPORTED_ENCODING);
                }
            } else {
                throw new IncorrectOutputException("Converter unable to convert LINE into assigned output data format: ".concat(String.valueOf(outputType)));
            }

        } else if (inputType == ServiceConstants.DATA_CLARK_TOKENS || inputType == ServiceConstants.DATA_CLARK_TAGS) {

            if (outputType == ServiceConstants.DATA_LINE) {
                return Converter.clark2line((Document) input, EOS_TOKEN);
            } else if (outputType == ServiceConstants.DATA_GAZE) {
                return Converter.line2gaze(Converter.clark2line((Document) input, EOS_TOKEN), EOS_TOKEN);
            } else if (outputType == ServiceConstants.DATA_CONLL) {
                return Converter.clark2conll((Document) input, CONLL_MAP);
            } else if (outputType == ServiceConstants.DATA_WEBLICHT) {
                return new WebLicht(ClarkDocumentBuilder.buildClarkDocument((String)input));
            } else {
                throw new IncorrectOutputException("Converter unable to convert CLARK_TOKENS/CLARK_TAGS into assigned output data format: ".concat(String.valueOf(outputType)));
            }

        } else if (inputType == ServiceConstants.DATA_CONLL) {// This does not work yet. Needs a bidirectional tag-features map.

            if (outputType == ServiceConstants.DATA_LINE) {
                Converter.conll2line((Conll) input, EOS_TOKEN, CONLL_MAP_INV);
            } else if (outputType == ServiceConstants.DATA_GAZE) {
                Converter.line2gaze(Converter.conll2line((Conll) input, EOS_TOKEN, CONLL_MAP_INV), EOS_TOKEN);
            } else if (outputType == ServiceConstants.DATA_CLARK_TOKENS || outputType == ServiceConstants.DATA_CLARK_TAGS) {
                return Converter.conll2clark((Conll) input, CONLL_MAP_INV);
            } else if (outputType == ServiceConstants.DATA_WEBLICHT) {
                return ((Conll)input).toWebLicht(CONLL_MAP_INV);
            } else {
                throw new IncorrectOutputException("Converter unable to convert CONLL into assigned output data format: ".concat(String.valueOf(outputType)));
            }

        } else if (inputType == ServiceConstants.DATA_WEBLICHT) {

            WebLicht doc = new WebLicht((String)input);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                if (outputType == ServiceConstants.DATA_LINE) {
                    doc.toLines(baos, EOS_TOKEN, false);
                    return baos.toString(ServiceConstants.PIPE_CHARACTER_ENCODING);
                } else if (outputType == ServiceConstants.DATA_GAZE) {
                    doc.toLines(baos, EOS_TOKEN, true);
                    Converter.line2gaze(baos.toString(ServiceConstants.PIPE_CHARACTER_ENCODING), EOS_TOKEN);
                } else if (outputType == ServiceConstants.DATA_CLARK_TOKENS || outputType == ServiceConstants.DATA_CLARK_TAGS) {
                    return doc.toClark();
                } else if (outputType == ServiceConstants.DATA_CONLL) {
                    return doc.toConll(CONLL_MAP);
                } else {
                    throw new IncorrectOutputException("Converter unable to convert WEBLICHT into assigned output data format: ".concat(String.valueOf(outputType)));
                }
            } catch (UnsupportedEncodingException ex) {
                logger.severe(ServiceConstants.EXCEPTION_UNSUPPORTED_ENCODING);
            }

        } else {
            throw new IncorrectInputException("Converter unable to convert input data: ".concat(String.valueOf(inputType)));
        }

        return null;

    }

    /**
     * Converts Line encoded data stream into CoNLL encoded data stram.
     * 
     * @param   is  Line encoded data <code>InputStream</code>
     * @param   os  CoNLL encoded data <code>OutputStream</code>
     * @param   eosToken    end of sentence token
     * @param   conllMap    <code>Map</code> connecting the original BTB tags to
     *                      their CoNLL versions.
     */
    public static void line2conll(InputStream is, OutputStream os, String eosToken, Properties conllMap) {
        
        LineConverter.toConll(is, os, eosToken, conllMap);

    }

    /**
     * Converts Line encoded data into CoNLL and returns a {@link Conll} object.
     * 
     * @param   lines   Line encoded data
     * @param   eosToken    end of sentence token
     * @param   conllMap    <code>Map</code> connecting the original BTB tags to
     *                      their CoNLL versions.
     * @return  {@link Conll}
     */
    public static Conll line2conll(String lines, String eosToken, Properties conllMap) {

        return LineConverter.toConll(lines, eosToken, conllMap);

    }

    /**
     * Converts Line encoded data stream into Gaze data stram.
     * 
     * @param   is  Line encoded data <code>InputStream</code>
     * @param   os  Gaze data <code>OutputStream</code>
     * @param   eosToken    end of sentence token
     */
    public static void line2gaze(InputStream is, OutputStream os, String eosToken) {
        
        LineConverter.toGaze(is, os, eosToken);
        
    }

    /**
     * Converts Line encoded data into Gaze data and returns a 
     * {@link String} object.
     * 
     * @param   lines   Line encoded data
     * @param   eosToken    end of sentence token
     * 
     * @return {@link String} - Line encoded data
     */
    public static String line2gaze(String lines, String eosToken) {

        return LineConverter.toGaze(lines, eosToken);

    }

     /**
     * Converts Line encoded data stream into CLaRK document and prints it into
     * the <code>OutputStream</code>.
     * 
     * @param   is  Line encoded data <code>InputStream</code>
     * @param   os  CLaRK document <code>OutputStream</code>
     * @param   eosToken    end of sentence token
     */
    public static void line2clark(InputStream is, OutputStream os, String eosToken) {
        
        LineConverter.toClark(is, os, eosToken);
        
    }

    /**
     * Converts Line encoded data into CLaRK document and returns a 
     * {@link org.w3c.dom.Document} object.
     * 
     * @param   lines   Line encoded data
     * @param   eosToken    end of sentence token
     * 
     * @return {@link org.w3c.dom.Document} - CLaRK document
     */
    public static Document line2clark(String lines, String eosToken) {

        return LineConverter.toClark(lines, eosToken);

    }

    /**
     * Converts {@link Conll} object read from the <code>InputStream</code> into
     * Line encoded data printed to the <code>OutputStream</code>.
     * 
     * @param   is   <code>InputStream</code> reading CoNLL object
     * @param   os   <code>OutputStream</code> with Line encoded data
     * @param   eosToken    end of sentence token
     * @param   iConllMap   <code>Map</code> containing connections between tags
     * in CoNLL representation and heir original forms
     */
    public static void conll2line(InputStream is, OutputStream os, String eosToken, Properties iConllMap) {
        
        Conll conll = new Conll(is);
        conll.toLine(os, eosToken, iConllMap);

    }

    /**
     * Converts {@link Conll} into Line encoded data.
     * 
     * @param   conll   {@link Conll} object
     * @param   eosToken    end of sentence token
     * @param   iConllMap   <code>Map</code> containing connections between tags
     * in CoNLL representation and heir original forms
     * 
     * @return {@link String} - Line encoded data
     */
    public static String conll2line(Conll conll, String eosToken, Properties iConllMap) {
        return conll.toLine(eosToken, iConllMap);

    }
    
    /**
     * Converts {@link Conll} object read from the <code>InputStream</code> into
     * CLaRK document and prints it to the <code>OutputStream</code>.
     * 
     * @param   is   <code>InputStream</code> reading CoNLL object
     * @param   os   <code>OutputStream</code> containing CLaRK document
     * @param   iConllMap   <code>Map</code> containing connections between tags
     * in CoNLL representation and heir original forms
     */
    public static void conll2clark(InputStream is, OutputStream os, Properties iConllMap) {
        Conll conll = new Conll(is);
        conll.toClark(os, iConllMap);
        
    }

    /**
     * Converts {@link Conll} object into CLaRK document.
     * 
     * @param   conll   {@link Conll} object
     * @param   iConllMap   <code>Map</code> containing connections between tags
     * in CoNLL representation and heir original forms
     * 
     * @return {@link org.w3c.dom.Document} - CLaRK document
     */
    public static Document conll2clark(Conll conll, Properties iConllMap) {

        return conll.toClark(iConllMap);

    }

    /**
     * Converts {@link Conll} object read from the <code>InputStream</code> into
     * CLaRK document and prints it to the <code>OutputStream</code>.
     * 
     * @param   is   <code>InputStream</code> reading the CLaRK document
     * @param   os   <code>OutputStream</code> with Line encoded data
     * @param   eosToken    end of sentence token
     */
    public static void clark2line(InputStream is, OutputStream os, String eosToken) {
        
        ClarkConverter.toLine(is, os, eosToken);
        
    }

    /**
     * Converts CLaRK document object into Line encoded data.
     * 
     * @param   doc   CLaRK document
     * @param   eosToken    end of sentence token
     * 
     * @return {@link String} - Line encoded data
     */
    public static String clark2line(Document doc, String eosToken) {

        return ClarkConverter.toLine(doc, eosToken);

    }

    /**
     * Converts CLaRK document read from the <code>InputStream</code> into
     * {@link Conll} object and prints it to the <code>OutputStream</code>.
     * 
     * @param   is   <code>InputStream</code> reading CoNLL object
     * @param   os   <code>OutputStream</code> containing CLaRK document
     * @param   conllMap   <code>Map</code> containing connections between tags
     * in BTB form and their CoNLL representation.
     */
    public static void clark2conll(InputStream is, OutputStream os, Properties conllMap) {
        
        ClarkConverter.toConll(is, os, conllMap);
        
    }

    /**
     * Converts CLaRK document object into Line encoded data.
     * 
     * @param   doc   CLaRK document
     * @param   conllMap   <code>Map</code> containing connections between tags
     * in BTB form and their CoNLL representation.
     * 
     * @return {@link Conll}
     */
    public static Conll clark2conll(Document doc, Properties conllMap) {

        return ClarkConverter.toConll(doc, conllMap);

    }

}
