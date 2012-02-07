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

import de.dspin.data.textcorpus.Token;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import org.bultreebank.labpipe.converters.Converter;
import org.bultreebank.labpipe.data.ClarkDocumentBuilder;
import org.bultreebank.labpipe.data.Conll;
import org.bultreebank.labpipe.data.WebLicht;
import org.bultreebank.labpipe.exceptions.ClarkConfigurationException;
import org.bultreebank.labpipe.exceptions.IncorrectInputException;
import org.bultreebank.labpipe.exceptions.IncorrectOutputException;
import org.bultreebank.labpipe.exceptions.IncorrectParameterValueException;
import org.bultreebank.labpipe.exceptions.MissingContentException;
import org.bultreebank.labpipe.exceptions.SVMTConnectionExceptoin;
import org.bultreebank.labpipe.utils.ClassMap;
import org.bultreebank.labpipe.utils.Configuration;
import org.bultreebank.labpipe.utils.Misc;
import org.bultreebank.labpipe.utils.ServiceConstants;
import org.bultreebank.labpipe.utils.XmlUtils;
import org.maltparser.core.exception.MaltChainedException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * <code>ProcessingLine</code> is the central command class of LABPipe. It 
 * initializes all tools and keeps them in the memory while waiting for data and
 * processing commands. An instance of this class may import, process (with a 
 * given list of pipe commands) and export data an arbitrary number of times, 
 * while keeping the same processing tools in the memory.
 *
 * @author Aleksandar Savkov
 */
public class ProcessingLine {

    /* Logger */
    private static final Logger logger = Logger.getLogger(ProcessingLine.class.getName());
    private WebLicht DOC = new WebLicht();
    private Object DATA;
    private int DATA_TYPE;
    /* Tokenizers */
    private FstTokenizer FST_TOKENIZER;
    /**
     * @deprecated since v1.0
     */
    private RegExTokenizer RE_TOKENIZER;
    /* --Escape sequences-- */
    /* 
     * Escape sequences need to be marked during tokenization or data input for
     * the purpose of giving direct internal commands to some of the components 
     * and/or skipping character ssequences.
     */
    private HashMap<Token, List<String>> ESCAPES;
    /* Sentence Detector */
    private SentenceDetector SENT_DETECTOR;
    /* Configuration objects */
    private Configuration OPTIONS = new Configuration();
    private Configuration CONLL_MAP = new Configuration();
    private Configuration CONLL_MAP_INV = new Configuration();

    /* CLaRK annotators */
    private ClarkAnnotation CONSTRAINTS;
    private ClarkAnnotation TOK_AND_SENT;
    private ClarkAnnotation TAG;
    private ClarkAnnotation LEMMA;
    /* Malt Parser */
    private MaltParserWrapper MALT_PARSER;
    /* Commands List */
    private List<Integer> COMMANDS = null;
    /* Data Format Converter */
    private Converter CONVERTER;
    
    private boolean VERBOSE = false;
    private OutputStream DEFAULT_OUTPUT_STREAM = System.out;
    private int DEFAULT_OUTPUT_FORMAT = ServiceConstants.DATA_CONLL;

    /**
     * Creates a new empty object
     */
    public ProcessingLine() {
    }

    /**
     * Creates a new object based on the LABPipe configuration provided in <code>options</code>
     * 
     * @param   options LABPipe configuration
     * @throws IncorrectInputException 
     * @throws IncorrectOutputException 
     * @throws MaltChainedException 
     * @throws JAXBException 
     * @throws ClarkConfigurationException
     * @throws IOException  
     */
    public ProcessingLine(Configuration options) throws IncorrectInputException, IncorrectOutputException, MaltChainedException, JAXBException, IOException, ClarkConfigurationException {

        OPTIONS = options;
        buildProcessingLine();

    }

    /**
     * Builds the processing line by initializing all tools and loading all configurations.
     * 
     * @throws IncorrectInputException 
     * @throws IncorrectOutputException 
     * @throws JAXBException 
     * @throws MaltChainedException 
     * @throws ClarkConfigurationException
     * @throws IOException  
     */
    public final void buildProcessingLine() throws IncorrectInputException, IncorrectOutputException, JAXBException, MaltChainedException, IOException, ClarkConfigurationException {

        System.out.println("Firing up LABPipe...");
        System.out.println("-------------------------------------------------------");
        
        CONVERTER = new Converter(OPTIONS);

        RE_TOKENIZER = new RegExTokenizer(OPTIONS);

        FST_TOKENIZER = new FstTokenizer(OPTIONS);

        CONLL_MAP.loadConfigFileFromFS(OPTIONS.getConllMapPath(), Configuration.PROPS);
        CONLL_MAP_INV.loadConfigFileFromFS(OPTIONS.getConllMapInvPath(), Configuration.PROPS);

        // Initiating CLaRK Constraints
        System.out.println("Loading CLaRK processors...");
        CONSTRAINTS = new ClarkAnnotation(OPTIONS, "constraintsQuery");
        TOK_AND_SENT = new ClarkAnnotation(OPTIONS, "tokAndSentQuery");
        TAG = new ClarkAnnotation(OPTIONS, "tagQuery");
        LEMMA = new ClarkAnnotation(OPTIONS, "lemmaQuery");

        // Initilizing MaltParser Service
        System.out.println("Loading MaltParser service...");
        MALT_PARSER = new MaltParserWrapper(OPTIONS);
        
        COMMANDS = OPTIONS.getDefaultPipe();
        
        System.out.println("-------------------------------------------------------");
        System.out.println("LABPipe fired up!");

    }

    /**
     * Loads LABPipe configuration from a file on the file system
     * 
     * @param   configPath  file path on the file system
     * @param   fileType    configuration file type
     * @throws IOException  
     */
    public void loadOptions(String configPath, int fileType) throws IOException {

        OPTIONS.loadConfigFileFromFS(configPath, fileType);

    }

    /**
     * Loads LABPipe configuration from a file on a server
     * 
     */
    public void loadOptionsFromServer() {

        OPTIONS.loadConfigFileFromServer(ServiceConstants.CONFIG_PATH_SRV, Configuration.XML);

    }

    /**
     * Imports input data from stream
     * 
     * @param   is  input stream
     * @param   dataType    input data type
     * @throws IncorrectInputException  
     */
    public void importInput(InputStream is, int dataType) throws IncorrectInputException {

        System.out.print("Importing data...");
        if (dataType == ServiceConstants.DATA_TEXT || dataType == ServiceConstants.DATA_LINE || dataType == ServiceConstants.DATA_GAZE) {
            DATA = (is instanceof FileInputStream) ? Misc.readFileInputStream(is) : Misc.readInputStream(is);
        } else if (dataType == ServiceConstants.DATA_CONLL) {
            DATA = new Conll(is);
        } else if (dataType == ServiceConstants.DATA_CLARK_TOKENS || dataType == ServiceConstants.DATA_CLARK_TAGS) {
            DATA = ClarkDocumentBuilder.buildClarkDocument(is);
        } else if (dataType == ServiceConstants.DATA_WEBLICHT) {
            DOC.unmarshall(is);
            DATA = DOC;
        } else {
            throw new IncorrectInputException("The provided data type is not supported by the system. Data cannot be imported.");
        }

        DATA_TYPE = dataType;
        
        System.out.println("done");

    }

    /**
     * Inputs data from <code>String</code>
     * 
     * @param   input   input String
     * @param   dataType    input data type
     * @throws IOException 
     * @throws MissingContentException
     * @throws ParserConfigurationException
     * @throws JAXBException 
     * @throws IncorrectInputException 
     * @throws SAXException  
     */
    public void importInput(String input, int dataType) throws IOException, ParserConfigurationException, SAXException, JAXBException, IncorrectInputException, MissingContentException {

        System.out.print("Importing data...");
        if (dataType == ServiceConstants.DATA_TEXT || dataType == ServiceConstants.DATA_LINE || dataType == ServiceConstants.DATA_GAZE) {
            DATA = input;
        } else if (dataType == ServiceConstants.DATA_CONLL) {
            DATA = new Conll(input);
        } else if (dataType == ServiceConstants.DATA_CLARK_TOKENS || dataType == ServiceConstants.DATA_CLARK_TAGS) {
            DATA = ClarkDocumentBuilder.buildClarkDocument(input);
        } else if (dataType == ServiceConstants.DATA_WEBLICHT) {
            DOC.unmarshall(input);
            DATA = DOC;
        } else {
            throw new IncorrectInputException("The provided data type is not supported by the system. Data cannot be imported.");
        }

        DATA_TYPE = dataType;
        System.out.println("done");

    }

    /**
     * Tokenize the current working data using the default tokenizer
     * @throws IncorrectInputException
     * @throws InterruptedException
     * @throws IncorrectParameterValueException 
     * @throws MissingContentException 
     * @throws ClarkConfigurationException  
     */
    public void tokenize() throws IncorrectInputException, InterruptedException, IncorrectParameterValueException, MissingContentException, ClarkConfigurationException {

        tokenize(ServiceConstants.PIPE_SFST_TOKENIZE);

    }

    /**
     * Tokenize the current working data using a selected tokenizer identified by <code>tokenizer</code>.
     * 
     * @param   tokenizer   tokenizer
     * @throws IncorrectInputException
     * @throws InterruptedException 
     * @throws IncorrectParameterValueException
     * @throws ClarkConfigurationException
     * @throws MissingContentException  
     */
    public void tokenize(int tokenizer) throws IncorrectInputException, InterruptedException, IncorrectParameterValueException, MissingContentException, ClarkConfigurationException {

        if (DATA_TYPE != ServiceConstants.DATA_TEXT) {
            throw new IncorrectInputException("Incorrect input suppied to tokenizer.");
        }

        if (tokenizer == ServiceConstants.PIPE_SFST_TOKENIZE) {
            System.out.print("Tokenizing with SFST...");
            DATA = FST_TOKENIZER.tokenize((String) DATA);
            DATA_TYPE = ServiceConstants.DATA_LINE;
        } else if (tokenizer == ServiceConstants.PIPE_CLARK_TOKENIZE) {
            System.out.print("Tokenizing with CLaRK (deprecated)...");
            DATA = ClarkDocumentBuilder.buildClarkDocument(TOK_AND_SENT.annotateTextData((String) DATA));
            DATA_TYPE = ServiceConstants.DATA_CLARK_TOKENS;
        } else if (tokenizer == ServiceConstants.PIPE_REGEX_TOKENIZE) {
            System.out.print("Tokenizing with RegEx (deprecated)...");
            DATA = RE_TOKENIZER.tokenize((String) DATA);
            DATA_TYPE = ServiceConstants.DATA_LINE;
        } else {
            throw new IncorrectParameterValueException(String.valueOf(tokenizer));
        }

        System.out.println("done");

    }

    /**
     * @throws IncorrectInputException 
     * @deprecated since v1.0
     */
    public void splitSentences() throws IncorrectInputException {
        splitSentences(1);
    }

    /**
     * @param splitter 
     * @throws IncorrectInputException 
     * @deprecated since v1.0
     */
    public void splitSentences(int splitter) throws IncorrectInputException {

        if (splitter == 1) {
            SENT_DETECTOR.detectSentenceBoundaries(DOC);
        }


    }

    /**
     * POS-tags the current working data using the default POS tagger
     * @throws IncorrectInputException 
     * @throws IncorrectParameterValueException
     * @throws SVMTConnectionExceptoin 
     * @throws ClarkConfigurationException
     * @throws MissingContentException 
     * @throws IncorrectOutputException  
     */
    public void tag() throws IncorrectInputException, SVMTConnectionExceptoin, MissingContentException, IncorrectOutputException, IncorrectParameterValueException, ClarkConfigurationException {

        tag(ServiceConstants.PIPE_SVMTOOL_TAG);

    }

    /**
     * POS-tags the current working data using a selected POS tagger identified by <code>tagger</code>
     * 
     * @param   tagger  selected POS tagger
     * @throws IncorrectInputException
     * @throws SVMTConnectionExceptoin 
     * @throws MissingContentException 
     * @throws IncorrectOutputException
     * @throws ClarkConfigurationException
     * @throws IncorrectParameterValueException  
     */
    public void tag(int tagger) throws IncorrectInputException, SVMTConnectionExceptoin, MissingContentException, IncorrectOutputException, IncorrectParameterValueException, ClarkConfigurationException {

        if (DATA == null) {
            throw new IncorrectInputException("Pipe failed to supply LINE data format to the tagger.");
        }

        if (tagger == ServiceConstants.PIPE_SVMTOOL_TAG) {
            System.out.print("Tagging with SVMTool...");
            DATA = CONVERTER.convert(DATA, DATA_TYPE, ServiceConstants.DATA_LINE);
            if (DATA == null) {
                throw new IncorrectInputException("Pipe failed to supply LINE data format to the tagger.");
            } else {
                DATA_TYPE = ServiceConstants.DATA_LINE;
            }
            DATA = SVMTagger.tagLinesString((String) DATA, OPTIONS);
            DATA_TYPE = ServiceConstants.DATA_LINE;
        } else if (tagger == ServiceConstants.PIPE_GAZE_TAG) {
            System.out.print("Tagging with Gaze...");
            DATA = CONVERTER.convert(DATA, DATA_TYPE, ServiceConstants.DATA_GAZE);
            if (DATA == null) {
                throw new IncorrectInputException("Pipe failed to supply GAZE data format to the tagger.");
            } else {
                DATA_TYPE = ServiceConstants.DATA_LINE;
            }
            DATA = GazeTagger.tagString((String) DATA, OPTIONS);
            DATA_TYPE = ServiceConstants.DATA_LINE;
        } else if (tagger == ServiceConstants.PIPE_CLARK_TAG) {
            System.out.print("Tagging with CLaRK...");
            DATA = CONVERTER.convert(DATA, DATA_TYPE, ServiceConstants.DATA_CLARK_TOKENS);
            if (DATA == null) {
                throw new IncorrectInputException("Pipe failed to supply CLARK_TOKENS data format to the tagger.");
            } else {
                DATA_TYPE = ServiceConstants.DATA_LINE;
            }
            DATA = TAG.processXmlDocument((Document) DATA);
            DATA_TYPE = ServiceConstants.DATA_CLARK_TAGS;
        } else {
            throw new IncorrectParameterValueException("Incorrect tagger: " + tagger);
        }
        
        System.out.println("done");

    }
    
    /**
     * Lemmatizes the current working data with the default lemmatizer
     * @throws MissingContentException 
     * @throws IncorrectInputException
     * @throws IncorrectOutputException
     * @throws IncorrectParameterValueException
     * @throws ClarkConfigurationException  
     */
    public void lemmatize() throws MissingContentException, IncorrectInputException, IncorrectOutputException, IncorrectParameterValueException, ClarkConfigurationException {

        lemmatize(ServiceConstants.PIPE_CLARK_LEMMATIZE);

    }

    /**
     * Lemmatizes the current working data with a selected lemmatizer identified by <code>lemmatizer</code>.
     * 
     * @param   lemmatizer  selected lemmatizer
     * @throws MissingContentException 
     * @throws IncorrectInputException
     * @throws IncorrectParameterValueException
     * @throws IncorrectOutputException
     * @throws ClarkConfigurationException  
     */
    public void lemmatize(int lemmatizer) throws MissingContentException, IncorrectInputException, IncorrectOutputException, IncorrectParameterValueException, ClarkConfigurationException {

        DATA = CONVERTER.convert(DATA, DATA_TYPE, ServiceConstants.DATA_CLARK_TAGS);

        if (DATA == null) {
            throw new IncorrectInputException("Pipe failed to supply CLARK_TAGS data format to the lemmatizer.");
        } else {
            DATA_TYPE = ServiceConstants.DATA_CLARK_TAGS;
        }

        if (lemmatizer == ServiceConstants.PIPE_CLARK_LEMMATIZE) {

            System.out.print("Lemmatizing with CLaRK...");
            DATA = LEMMA.processXmlDocument((Document) DATA, "laska.dtd");

        } else {

            throw new IncorrectParameterValueException("Lemmatizer does not exist");

        }

        DATA_TYPE = ServiceConstants.DATA_CLARK_TAGS;
        System.out.println("done");

    }

    /**
     * Corrects POS tags with the default corrector. This is also referred to as
     * CLaRK tagging.
     * @throws MissingContentException 
     * @throws IncorrectInputException 
     * @throws IncorrectOutputException 
     * @throws ClarkConfigurationException
     * @throws IncorrectParameterValueException  
     */
    public void correct() throws MissingContentException, IncorrectInputException, IncorrectOutputException, IncorrectParameterValueException, ClarkConfigurationException {
        correct(ServiceConstants.PIPE_CLARK_CORRECT);
    }

    /**
     * Corrects POS tags with a selected corrector
     * 
     * @param   corrector   selected corrector
     * @throws MissingContentException 
     * @throws IncorrectInputException 
     * @throws IncorrectOutputException
     * @throws IncorrectParameterValueException 
     * @throws ClarkConfigurationException  
     */
    public void correct(int corrector) throws MissingContentException, IncorrectInputException, IncorrectOutputException, IncorrectParameterValueException, ClarkConfigurationException {

        DATA = CONVERTER.convert(DATA, DATA_TYPE, ServiceConstants.DATA_CLARK_TAGS);

        if (DATA == null) {
            throw new IncorrectInputException("Pipe failed to supply CLARK_TAGS data format to the morphological corrector.");
        } else {
            DATA_TYPE = ServiceConstants.DATA_CLARK_TAGS;
        }

        if (corrector == ServiceConstants.PIPE_CLARK_CORRECT) {

            System.out.print("Applying morphological rules...");
            DATA = CONSTRAINTS.processXmlDocument((Document) DATA, "laska.dtd");

        } else {

            throw new IncorrectParameterValueException("Corrector does not exist");

        }

        DATA_TYPE = ServiceConstants.DATA_CLARK_TAGS;
        System.out.println("done");

    }

    /**
     * Dependency parsing with the default Dependency Parser.
     * @throws MaltChainedException 
     * @throws MissingContentException
     * @throws IncorrectInputException
     * @throws IncorrectOutputException  
     */
    public void parse() throws MaltChainedException, IncorrectInputException, MissingContentException, IncorrectOutputException {

        parse(ServiceConstants.PIPE_MALTPARSER_PARSE);

    }

    /**
     * Dependency parsing with a selected dependency parser <code>parser</code>
     * @param parser
     * @throws MaltChainedException 
     * @throws IncorrectInputException 
     * @throws MissingContentException
     * @throws IncorrectOutputException  
     */
    public void parse(int parser) throws MaltChainedException, IncorrectInputException, MissingContentException, IncorrectOutputException {

        DATA = CONVERTER.convert(DATA, DATA_TYPE, ServiceConstants.DATA_CONLL);

        if (DATA == null) {
            throw new IncorrectInputException("Pipe failed to supply CONLL data format to the morphological corrector.");
        } else {
            DATA_TYPE = ServiceConstants.DATA_CONLL;
        }

        if (parser == ServiceConstants.PIPE_MALTPARSER_PARSE) {
            System.out.print("Dependency parsing with MaltParser...");
            DATA = MALT_PARSER.parseConll((Conll) DATA);

        }

        DATA_TYPE = ServiceConstants.DATA_CONLL;
        System.out.println("done");

    }

    /**
     * Runs the processing line with the specified <code>commands</code>
     * 
     * @param   commands    list of processing commands
     * @throws IncorrectInputException 
     * @throws InterruptedException 
     * @throws ClarkConfigurationException
     * @throws IncorrectParameterValueException
     * @throws MissingContentException 
     * @throws IncorrectOutputException 
     * @throws SVMTConnectionExceptoin
     * @throws MaltChainedException  
     */
    public void run(List<Integer> commands) throws IncorrectInputException, InterruptedException, IncorrectParameterValueException, MissingContentException, SVMTConnectionExceptoin, IncorrectOutputException, MaltChainedException, ClarkConfigurationException {

        COMMANDS = commands;
        run();

    }

    /**
     * Runs the processing line with on the current working data and using the 
     * current pipe commands. In case the pipe commands are not initialized the 
     * pipe uses the default configuration from the LABPipe configuration file.
     * @throws IncorrectInputException 
     * @throws InterruptedException 
     * @throws IncorrectParameterValueException 
     * @throws MissingContentException 
     * @throws IncorrectOutputException
     * @throws MaltChainedException
     * @throws SVMTConnectionExceptoin
     * @throws ClarkConfigurationException  
     */
    public void run() throws IncorrectInputException, InterruptedException, IncorrectParameterValueException, MissingContentException, SVMTConnectionExceptoin, IncorrectOutputException, MaltChainedException, ClarkConfigurationException {

        if (COMMANDS == null) {
            throw new NullPointerException("Processing commands list is empty or not initilized.");
        }
        
        System.out.println("Running LABPipe commands...");
        System.out.println("-------------------------------------------------------");
        
        for (int c : COMMANDS) {
            switch (c) {
                case ServiceConstants.PIPE_SFST_TOKENIZE:
                    this.tokenize();
                    break;
                case ServiceConstants.PIPE_REGEX_TOKENIZE:
                    this.tokenize(ServiceConstants.PIPE_REGEX_TOKENIZE);
                    break;
                case ServiceConstants.PIPE_CLARK_TOKENIZE:
                    this.tokenize(ServiceConstants.PIPE_CLARK_TOKENIZE);
                    break;
                case ServiceConstants.PIPE_SVMTOOL_TAG:
                    this.tag();
                    break;
                case ServiceConstants.PIPE_GAZE_TAG:
                    this.tag(ServiceConstants.PIPE_GAZE_TAG);
                    break;
                case ServiceConstants.PIPE_CLARK_TAG:
                    this.tag(ServiceConstants.PIPE_CLARK_TAG);
                    break;
                case ServiceConstants.PIPE_CLARK_LEMMATIZE:
                    this.lemmatize();
                    break;
                case ServiceConstants.PIPE_CLARK_CORRECT:
                    this.correct();
                    break;
                case ServiceConstants.PIPE_MALTPARSER_PARSE:
                    this.parse();
                    break;
            }
            
            if (VERBOSE) {
                this.exportOutput(DEFAULT_OUTPUT_STREAM, DEFAULT_OUTPUT_FORMAT);
            }
            
        }
        
        System.out.println("-------------------------------------------------------");
        System.out.println("LABPipe commands: complete.");

    }

    /**
     * Exports output into <code>os</code>
     * 
     * @param   os  output stream
     * @param   dataType    output data type
     * @throws IncorrectOutputException
     * @throws IncorrectInputException 
     * @throws MissingContentException  
     */
    public void exportOutput(OutputStream os, int dataType) throws IncorrectOutputException, IncorrectInputException, MissingContentException {
        
        Object data = CONVERTER.convert(DATA, DATA_TYPE, dataType);
        
        System.out.print("Exporting data...");

        try {
            
            OutputStreamWriter osw = new OutputStreamWriter(os, ServiceConstants.PIPE_CHARACTER_ENCODING);
            
            if (dataType == ServiceConstants.DATA_LINE) {
                osw.write(((String) data));
            } else if (dataType == ServiceConstants.DATA_CONLL) {
                osw.write(((Conll) data).toString());
            } else if (dataType == ServiceConstants.DATA_CLARK_TAGS
                    || dataType == ServiceConstants.DATA_CLARK_TOKENS) {
                XmlUtils.print(((Document) data), os);
            } else if (dataType == ServiceConstants.DATA_WEBLICHT) {
                ((WebLicht) data).exportAsXML(os);
            }
            
            osw.flush();
            
        } catch (ClassCastException ex) {
            logger.log(Level.WARNING, "ClassCastException during output conversion.", ex);
            throw new IncorrectInputException("Unacceptable data format contained in the pipe. Data cannot be extracted.");
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_UNSUPPORTED_ENCODING, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_IO, ex);
        }
        
        System.out.println("done");

    }
    
    /**
     * 
     * @throws MissingContentException
     * @throws IncorrectInputException
     * @throws IncorrectOutputException
     */
    public String exportOutput(int dataType) throws MissingContentException, IncorrectInputException, IncorrectOutputException {
        Object data = CONVERTER.convert(DATA, DATA_TYPE, dataType);
        
        System.out.print("Exporting data...");

        try {
            
            if (dataType == ServiceConstants.DATA_LINE) {
                return (String)data;
            } else if (dataType == ServiceConstants.DATA_CONLL) {
                return ((Conll) data).toString();
            } else if (dataType == ServiceConstants.DATA_CLARK_TAGS
                    || dataType == ServiceConstants.DATA_CLARK_TOKENS) {
                XmlUtils.printToString(((Document) data));
            } else if (dataType == ServiceConstants.DATA_WEBLICHT) {
                return ((WebLicht) data).toString();
            }
            
        } catch (ClassCastException ex) {
            logger.log(Level.WARNING, "ClassCastException during output conversion.", ex);
            throw new IncorrectInputException("Unacceptable data format contained in the pipe. Data cannot be extracted.");
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_UNSUPPORTED_ENCODING, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_IO, ex);
        }
        
        return null;
    }

    /**
     * Gets the list of commands
     * 
     * @return  List&lt;Integer&gt;
     */
    public List<Integer> getCommands() {
        return COMMANDS;
    }

    /**
     * Sets the list of processing commands
     * 
     * @param   commands    list of processing commands
     */
    public void setCommands(List commands) {
        COMMANDS = commands;
    }

   /**
     * @deprecated since v1.0
     */
    public WebLicht getData() {

        return DOC;

    }

    /**
     * @param doc 
     * @deprecated since v1.0
     */
    public void setData(WebLicht doc) {

        DOC = doc;

    }

    /**
     * @deprecated since v1.0
     */
    public boolean containsData(Class c) {

        ClassMap cm = new ClassMap(DOC.getTextCorpus().getTextOrTokensOrSentences());

        if (cm.containsKey(c)) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * Clears current working data.
     */
    public void clear() {
        DOC = new WebLicht();
        DATA = null;
    }
    
    /**
     * Sets <code>verbose</code> mode.
     * 
     * Default is false
     * @param verbose 
     */
    public void setVerbose(boolean verbose) {
        VERBOSE = verbose;
    }
    
    /**
     * Sets the default output stream for the command line interaction.
     * 
     * Default is <code>System.out</code>
     * @param os 
     */
    public void setDefaultOutputStream(OutputStream os) {
        DEFAULT_OUTPUT_STREAM = os;
    }
    
    /**
     * Sets default output format for <code>verbose</code> mode data snapshots. 
     * 
     * Default is CoNLL
     * @param dof 
     */
    public void setDefaultOutputFormat(int dof) {
        DEFAULT_OUTPUT_FORMAT = dof;
    }

}
