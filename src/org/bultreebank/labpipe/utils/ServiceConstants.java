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

/**
 *
 * @author Aleksandar Savkov
 */
public class ServiceConstants {
    
    /* Server Constants */
    
    /**
     * Default path relative to the working directory containing the configuration file.
     */
    public static final String CONFIG_PATH_SRV = "conf/conf.xml";
    /**
     * Default URL of the <code>FileReader</code> servlet.
     * 
     * //TODO This URL may not be the best idea. Try maybe using a relative path.
     * 
     */
    public static final String FILE_READER_SERVLET_URL = "http://localhost:8080/SVMTWebTagger/servlet/FileReaderServlet";
    /**
     * Default System separator.
     */
    public static final String SYSTEM_SEPARATOR = System.getProperty("file.separator");
    
    /*
     * Tools Constants
     */
    public static final String ESCAPE_SEQUENCE_MODEL = "\\esc\\{[^}]\\}";
    public static final String SFST_EOS_TOKEN = "<eos>";
    public static final String PRE_SPACE_PUNCT_SIGNS_PATTERN = "[\\.,!?:;-\\]})]+";
    public static final String POST_SPACE_PUNCT_SIGNS_PATTERN = "[\\[{(]+";
    public static final String GAZE_EOS_TOKEN = "##sb";
    
    /* Data Formats */
    public final static int DATA_TEXT = 1;
    public final static int DATA_LINE = 2;
    public final static int DATA_WEBLICHT = 3;
    public final static int DATA_CONLL = 4;
    public final static int DATA_CLARK_TOKENS = 5;
    public final static int DATA_CLARK_TAGS = 6;
    public final static int DATA_GAZE = 7;
    
    /* Pipe Commands */
    public final static int PIPE_SFST_TOKENIZE = 1;
    public final static int PIPE_REGEX_TOKENIZE = 2;
    public final static int PIPE_SVMTOOL_TAG = 3;
    public final static int PIPE_CLARK_CORRECT = 4;
    public final static int PIPE_MALTPARSER_PARSE = 5;
    public final static int PIPE_GAZE_TAG = 6;
    public final static int PIPE_CLARK_TOKENIZE = 7;
    public final static int PIPE_CLARK_LEMMATIZE = 8;
    public final static int PIPE_CLARK_TAG = 9;
    
    /* Encoding */
    public final static String PIPE_CHARACTER_ENCODING = "UTF-8";
    
    /* Exception Messages*/
    public final static String EXCEPTION_UNSUPPORTED_ENCODING = "I feel sorry for you: it seems that your system does not support UTF-8.";
    public final static String EXCEPTION_JAXB = "There's probably something wrong with the WebLicht data. Check what generated it.";
    public final static String EXCEPTION_PARSER_CONFIGURATION = "This should not be happening! Check parsing XML files (mainly CLaRK XML format).";
    public final static String EXCEPTION_IO = "Check the few places where actual file writing occurs.";
    public final static String EXCEPTION_SAX_PARSER = "Check the few places where actual file writing occurs.";
    public final static String EXCEPTION_FILE_NOT_FOUND = "File not found: ";
    
    /* Properties */
    public final static String PROPS_KEY_VAL = "KEY_VAL";
    public final static String PROPS_XML = "XML";
    
}
