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
package org.bultreebank.labpipe.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bultreebank.labpipe.exceptions.IncorrectParameterValueException;

/**
 * <code>Configuration</code> is the universal control object used in LABPipe. 
 * It is usually loaded from a XML file on the file system or a server.
 *
 * @author Aleksandar Savkov
 */
public class Configuration extends Properties {

    private static final Logger logger = Logger.getLogger(Configuration.class.getName());
    /**
     * Configuration parameter: pipeHome
     * 
     * Indicates LABPipe home directory
     */
    public static final String PIPE_HOME = "pipeHome";
    /**
     * Configuration parameter: usePipeHome
     * 
     * Indicates if LABPipe home directory should be used for dissolving relative paths
     */
    public static final String USE_PIPE_HOME = "usePipeHome";
    /**
     * @deprecated since v1.0
     */
    public static final String PROPERTIES_SOURCE = "propertiesSource";
    /**
     * @deprecated since v1.0
     */
    public static final String PROPERTIES_SOURCE_FS = "fs";
    /**
     * @deprecated since v1.0
     */
    public static final String PROPERTIES_SOURCE_SRV = "srv";
    /**
     * Configuration parameter: defaultPipe
     * 
     * List of pipe commands to be executed in case of empty commands list
     */
    public static final String DEFAULT_PIPE = "defaultPipe";
    /**
     * Configuration parameter: baseUrl
     * 
     * Base URL of the deployed web project (WEB)
     */
    public static final String BASE_URL = "baseUrl";
    /**
     * Configuration parameter: tomcatDir
     * 
     * Path to Tomcat on the file system (WEB)
     */
    public static final String TOMCAT_DIR = "tomcatDir";
    /**
     * Configuration parameter: projectDir
     * 
     * Path to the web project from the Tomcat base directory (WEB)
     */
    public static final String PROJECT_DIR = "projectDir";
    /**
     * Configuration parameter: tmpDir
     * 
     * Temporary directory path
     */
    public static final String TMP_DIR = "tmpDir";
    /**
     * Configuration parameter: storeDir
     * 
     * Path to store directory inside a web project, usually WEB-INF (WEB)
     */
    public static final String STORE_DIR = "storeDir";
    /**
     * Configuration parameter: tmpUrl
     * 
     * URL of temp directory in a web project (WEB)
     */
    public static final String TMP_URL = "tmpUrl";
    /**
     * Configuration parameter: tokenPattern
     * 
     * RegEx definition of a word token used in the {@link org.bultreebank.labpipe.tools.RegExTokenizer}
     */
    public static final String TOKEN_PATTERN = "tokenPattern";
    /**
     * Configuration parameter: punctPattern
     * 
     * RegEx definition of a punctuation sign used in the {@link org.bultreebank.labpipe.tools.RegExTokenizer}
     */
    public static final String PUNCT_PATTERN = "punctPattern";
    /**
     * Configuration parameter: tokenizationExceptionsList
     * 
     * List of tokenization exceptions used in the {@link org.bultreebank.labpipe.tools.RegExTokenizer}
     */
    public static final String TOKENIZATION_EXCEPTIONS_LIST = "tokenizationExceptionsList";
    /**
     * Configuration parameter: sfstCommand
     * 
     * Path to <code>sfst-match</code> used in {@link org.bultreebank.labpipe.tools.FstTokenizer}
     */
    public static final String SFST_COMMAND = "sfstCommand";
    /**
     * Configuration parameter: transducerHome
     * 
     * Path to the transducers used in {@link org.bultreebank.labpipe.tools.FstTokenizer}
     */
    public static final String TRANSDUCER_HOME = "transducerHome";
    /**
     * Configuration parameter: transducerScript
     * 
     * Path to the transducer shell script executing all transducers in a pipe
     * @deprecated since v1.0
     */
    public static final String TRANSDUCER_SCRIPT = "transducerScript";
    /**
     * Configuration parameter: transducerList
     * 
     * List of transducers used in {@link org.bultreebank.labpipe.tools.FstTokenizer}
     */
    public static final String TRANSDUCER_LIST = "transducerList";
    /**
     * Configuration parameter: clarkDir
     * 
     * CLaRK home directory
     */
    public static final String CLARK_DIR = "clarkDir";
    /**
     * Configuration parameter: clarkDtd
     * 
     * CLaRK default DTD
     */
    public static final String CLARK_DTD = "clarkDtd";
    /**
     * Configuration parameter: tokAndSentQuery
     * 
     * CLaRK tokenization and sentence detection multi-query
     */
    public static final String CLARK_TOK_SENT_QUERY_NAME = "tokAndSentQuery";
    /**
     * Configuration parameter: constraintsQuery
     * 
     * CLaRK morphological corrections and lemmatization multi-query
     */
    public static final String CLARK_CONSTRAINTS_QUERY_NAME = "constraintsQuery";
    /**
     * Configuration parameter: lemmaQuery
     * 
     * CLaRK lemmatization multi-query
     */
    public static final String CLARK_LEMMAS_QUERY_NAME = "lemmaQuery";
    /**
     * Configuration parameter: tagQuery
     * 
     * CLaRK POS-tags correction multi-query
     */
    public static final String CLARK_TAGS_QUERY_NAME = "tagQuery";
    /**
     * Configuration parameter: svmtUrl
     * 
     * SVMTool HTTP server URL (including port)
     */
    public static final String SVMT_URL = "svmtUrl";
    /**
     * @deprecated since v1.0
     */
    public static final String BOUNDARY_SIGNS = "boundarySigns";
    /**
     * Configuration parameter: noEos
     * 
     * Path to abbreviations dictionary containing only abbreviations appearing mid-sentence
     */
    public static final String NO_EOS_ABBR_DICT = "noEos";
    /**
     * Configuration parameter: possibleEos
     * 
     * Path to abbreviations dictionary containing only abbreviations that may appear in the end of a sentence
     */
    public static final String POSSIBLE_EOS_ABBR_DICT = "possibleEos";
    /**
     * Configuration parameter: namesDictionary
     * 
     * Names dictionary path
     */
    public static final String NAMES_DICT = "namesDictionary";
    /**
     * Configuration parameter: averageSentenceLength
     * 
     * Average sentence length (used in {@link org.bultreebank.labpipe.tools.RegExTokenizer})
     */
    public static final String AVG_SENT_LENGTH = "averageSentenceLength";
    /**
     * Configuration parameter: eosToken
     * 
     * End of sentence token
     */
    public static final String EOS_TOKEN = "eosToken";
    /**
     * Configuration parameter: conllMapPath
     * 
     * Path to BTB -> CoNLL <code>Map</code> <code>Properties</code> file (classic non-XML file)
     */
    public static final String CONLL_MAP_PATH = "conllMapPath";
    /**
     * Configuration parameter: conllMapInvPath
     * 
     * Path to CoNLL -> BTB <code>Map</code> <code>Properties</code> file (classic non-XML file)
     */
    public static final String CONLL_MAP_INV_PATH = "conllMapInvPath";
    /**
     * Configuration parameter: maltParserModel
     * 
     * Path to MaltParser model file
     */
    public static final String MALT_PARSER_MODEL = "maltParserModel";
    /**
     * Configuration parameter: maltWorkingDir
     * 
     * Path to MaltParser working directory
     */
    public static final String MALT_WORKING_DIR = "maltWorkingDir";
    /**
     * Configuration parameter: maltParsingAlgorithm
     * 
     * MaltParser algorithm
     */
    public static final String MALT_PARSING_ALGORITHM = "maltParsingAlgorithm";
    /**
     * Configuration parameter: maltRoot
     * 
     * Root value for the MaltParser command
     */
    public static final String MALT_ROOT = "maltRoot";
    /**
     * Configuration parameter: maltLiblinearOptions
     * 
     * MaltParser command LibLinear options
     */
    public static final String MALT_LIBLINEAR_OPTIONS = "maltLiblinearOptions";
    /**
     * Configuration parameter: maltFeatureModel
     * 
     * MaltParser feature model path
     */
    public static final String MALT_FEATURE_MODEL = "maltFeatureModel";
    /**
     * Configuration parameter: maltDsCol
     * 
     * MaltParser command DsCol
     */
    public static final String MALT_DS_COL = "maltDsCol";
    /**
     * Configuration parameter: maltDsDataStruct
     * 
     * MaltParser command DsDataStruct
     */
    public static final String MALT_DS_DATA_STRUC = "maltDsDataStruc";
    /**
     * Configuration parameter: maltDsDataStruct
     * 
     * MaltParser command DsDataStruct
     */
    public static final String MALT_DS_THRESHOLD = "maltDsThreshold";
    /**
     * Configuration parameter: maltMarkingStrategy
     * 
     * MaltParser command marking strategy
     */
    public static final String MALT_MARKING_STRATEGY = "maltMarkingStrategy";
    /**
     * Configuration parameter: mailHost
     * 
     * Mail host used in {@link Emailer}
     */
    public static final String MAIL_HOST = "mailHost";
    /**
     * Configuration parameter: mailFrom
     * 
     * Mail from host used in {@link Emailer}
     */
    public static final String MAIL_FROM = "mailFrom";
    /**
     * Configuration parameter: gazeDir
     * 
     * Gaze home directory
     */
    public static final String GAZE_DIR = "gazeDir";
    /**
     * @deprecated since v1.0
     */
    public static final String GAZE_ENCODING = "encoding";
    /**
     * @deprecated since v1.0
     */
    public static final String GAZE_CLASSPATH = "classpath";
    
    public static final String GAZE_XMX = "Xmx";
    /**
     * @deprecated since v1.0
     */
    public static final String GAZE_XMS = "Xms";
    /**
     * @deprecated since v1.0
     */
    public static final String GAZE_CLASS = "class";
    /**
     * @deprecated since v1.0
     */
    public static final String GAZE_ACTION = "action";
    /**
     * Configuration parameter: log4jconfig
     * 
     * Gaze log4j configuration file path
     */
    public static final String GAZE_LOG = "log4jconfig";
    /**
     * Configuration parameter: features
     * 
     * Gaze features file path
     */
    public static final String GAZE_FEATURES = "features";
    /**
     * Configuration parameter: tags
     * 
     * Gaze tags file path
     */
    public static final String GAZE_TAGS = "tags";
    /**
     * Configuration file type XML
     */
    public static final int XML = 1;
    /**
     * Configuration file type classic <code>Properties</code>
     */
    public static final int PROPS = 2;
    
    private String DIR_PIPE_HOME;
    private String DIR_CURRENT_PATH;
    private String DIR_TOMCAT_PATH;
    private String DIR_PROJECT_PATH;
    private String DIR_TMP_DIR_PATH;
    private String DIR_STORE_DIR_PATH;
    private String DIR_BASE_URL;
    private String DIR_TOK_EXCEPTIONS_PATH;
    private String DIR_TOK_NO_EOS_ABBR_DICT_PATH;
    private String DIR_TOK_POS_EOS_ABBR_DICT_PATH;
    private String DIR_NAMES_DICT_PATH;
    private String DIR_CONLL_MAP_PATH;
    private String DIR_CONLL_MAP_INV_PATH;
    private String DIR_GAZE_DIR;
    private String DIR_GAZE_LOG_CONF_PATH;
    private String DIR_GAZE_TAGS_PATH;
    private String DIR_GAZE_FEATURES_PATH;
    private String DIR_CLARK_HOME_PATH;
    private String DIR_SFST_COMMAND_PATH;
    private String DIR_SFST_TRANSDUCER_HOME_PATH;
    private String DIR_MALT_WORKING_DIR_PATH;

    /**
     * Constructs an empty <code>Configuration</code> object
     */
    public Configuration() {
    }

    /**
     * Constructs an object based on the configuration file at <code>path</code>
     * 
     * @param   path    configuration file path
     * @param   fileType    configuration file type
     * 
     */
    public Configuration(String path, int fileType) {

        this.loadConfigFileFromServer(path, fileType);

    }

    /**
     * Retrieves a property value after unescaping it using the methods in {@link EncodingConverter}
     * 
     * @param   key property key
     * 
     * @return  String  - unescaped property
     */
    @Override
    public String getProperty(String key) {

        return EncodingConverter.unescapeUnicode(super.getProperty(key));

    }

    /**
     * Loads a configuration file from the file system in this object
     * 
     * @param   path    path to configuration file
     * @param   fileType    configuration file type
     * 
     * @return {@link Configuration}    - this object
     */
    public final Configuration loadConfigFileFromFS(String path, int fileType) throws IOException {

        InputStream is = null;
        File in = new File(path);

        is = new FileInputStream(in);
        if (fileType == XML) {
            this.loadFromXML(is);
            this.trimProperties();
            this.buildDirsFS();
        } else {
            this.load(is);
            this.trimProperties();
        }

        is.close();

        return this;

    }

    /**
     * Loads a configuration file from a server in this object
     * 
     * @param   path    path to configuration file in {@link #STORE_DIR}
     * @param   fileType    configuration file type
     * 
     * @return {@link Configuration}    - this object
     */
    public final Configuration loadConfigFileFromServer(String path, int fileType) {

        String mType;

        try {

            switch (fileType) {
                case (PROPS):
                    mType = ServiceConstants.PROPS_KEY_VAL;
                    break;
                case (XML):
                    mType = ServiceConstants.PROPS_XML;
                    break;
                default:
                    throw new IncorrectParameterValueException("Invalid fileType parameter value: " + fileType);
            }

            // Construct data
            String data = URLEncoder.encode("path", ServiceConstants.PIPE_CHARACTER_ENCODING) + "=" + URLEncoder.encode("/WEB-INF/" + path, ServiceConstants.PIPE_CHARACTER_ENCODING) + "&" + URLEncoder.encode("type", ServiceConstants.PIPE_CHARACTER_ENCODING) + "=" + URLEncoder.encode(mType, ServiceConstants.PIPE_CHARACTER_ENCODING);

            // Send data
            URL url = new URL(ServiceConstants.FILE_READER_SERVLET_URL);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream(), ServiceConstants.PIPE_CHARACTER_ENCODING);
            osw.write(data);
            osw.flush();

            // Get the response
            switch (fileType) {
                case (XML):
                    this.loadFromXML(conn.getInputStream());
                    this.trimProperties();
                    this.buildDirsSrv();
                    break;
                case (PROPS):
                    this.load(conn.getInputStream());
                    this.trimProperties();
                    break;
            }

            osw.close();

        } catch (IncorrectParameterValueException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } catch (MalformedURLException ex) {
            logger.log(Level.SEVERE, "MalformedURLException in the ConfigReader. Check " + ServiceConstants.FILE_READER_SERVLET_URL, ex);
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.SEVERE, "UnsupportedEncodingException in the ConfigReader. Check UTF-8 support.", ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "IOException in the ConfigReader", ex);
        }

        return this;

    }

    /**
     * Builds directory paths on the server based on the configuration file
     * 
     */
    public void buildDirsSrv() {

        boolean usePipeHome = this.getProperty(Configuration.USE_PIPE_HOME).equals("true");

        DIR_PIPE_HOME = this.getProperty(Configuration.PIPE_HOME);
        DIR_TOMCAT_PATH = this.getProperty(Configuration.TOMCAT_DIR);
        DIR_PROJECT_PATH = DIR_TOMCAT_PATH + this.getProperty(Configuration.PROJECT_DIR);
        DIR_TMP_DIR_PATH = DIR_PROJECT_PATH + this.getProperty(Configuration.TMP_DIR);
        DIR_STORE_DIR_PATH = DIR_PROJECT_PATH + this.getProperty(Configuration.STORE_DIR);
        DIR_BASE_URL = this.getProperty(Configuration.BASE_URL);

        DIR_GAZE_DIR = (usePipeHome) ? DIR_PIPE_HOME + this.getProperty(Configuration.GAZE_DIR) : this.getProperty(Configuration.GAZE_DIR);
        DIR_GAZE_TAGS_PATH = (usePipeHome) ? DIR_GAZE_DIR + this.getProperty(Configuration.GAZE_TAGS) : this.getProperty(Configuration.GAZE_TAGS);
        DIR_GAZE_FEATURES_PATH = (usePipeHome) ? DIR_GAZE_DIR + this.getProperty(Configuration.GAZE_FEATURES) : this.getProperty(Configuration.GAZE_FEATURES);
        DIR_GAZE_LOG_CONF_PATH = (usePipeHome) ? DIR_STORE_DIR_PATH + this.getProperty(Configuration.GAZE_LOG) : this.getProperty(Configuration.GAZE_LOG);

        DIR_CLARK_HOME_PATH = (usePipeHome) ? DIR_PIPE_HOME + this.getProperty(Configuration.CLARK_DIR) : this.getProperty(Configuration.CLARK_DIR);
        DIR_SFST_COMMAND_PATH = (usePipeHome) ? DIR_PIPE_HOME + this.getProperty(Configuration.SFST_COMMAND) : this.getProperty(Configuration.SFST_COMMAND);
        DIR_SFST_TRANSDUCER_HOME_PATH = (usePipeHome) ? DIR_PIPE_HOME + this.getProperty(Configuration.TRANSDUCER_HOME) : this.getProperty(Configuration.TRANSDUCER_HOME);
        DIR_MALT_WORKING_DIR_PATH = (usePipeHome) ? DIR_PIPE_HOME + this.getProperty(Configuration.MALT_WORKING_DIR) : this.getProperty(Configuration.MALT_WORKING_DIR);

        DIR_TOK_EXCEPTIONS_PATH = DIR_STORE_DIR_PATH
                + this.getProperty(Configuration.TOKENIZATION_EXCEPTIONS_LIST);
        DIR_TOK_NO_EOS_ABBR_DICT_PATH = DIR_STORE_DIR_PATH
                + this.getProperty(Configuration.NO_EOS_ABBR_DICT);
        DIR_TOK_POS_EOS_ABBR_DICT_PATH = DIR_STORE_DIR_PATH
                + this.getProperty(Configuration.POSSIBLE_EOS_ABBR_DICT);
        DIR_NAMES_DICT_PATH = DIR_STORE_DIR_PATH
                + this.getProperty(Configuration.NAMES_DICT);
        DIR_CONLL_MAP_PATH = DIR_STORE_DIR_PATH
                + this.getProperty(Configuration.CONLL_MAP_PATH);
        DIR_CONLL_MAP_INV_PATH = DIR_STORE_DIR_PATH
                + this.getProperty(Configuration.CONLL_MAP_INV_PATH);

        if (!new File(DIR_TMP_DIR_PATH).exists()) {
            new File(DIR_TMP_DIR_PATH).mkdir();
        }

    }

    /**
     * Builds directory paths on the file system based on the configuration file
     * 
     */
    public void buildDirsFS() {

        try {
            DIR_PIPE_HOME = (this.containsKey(Configuration.PIPE_HOME)) ? this.getProperty(Configuration.PIPE_HOME) : new File(".").getCanonicalPath() + ServiceConstants.SYSTEM_SEPARATOR;
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Can't write file to the working directory!", ex);
            return;
        }

        boolean usePipeHome = this.getProperty(Configuration.USE_PIPE_HOME).equals("true");

        DIR_PROJECT_PATH = DIR_PIPE_HOME;
        DIR_TOMCAT_PATH = this.getProperty(Configuration.TOMCAT_DIR); // should not be used
        DIR_TMP_DIR_PATH = DIR_PIPE_HOME + this.getProperty(Configuration.TMP_DIR);
        DIR_STORE_DIR_PATH = DIR_PROJECT_PATH;
        DIR_BASE_URL = this.getProperty(Configuration.BASE_URL); // should not be used

        DIR_GAZE_DIR = (usePipeHome) ? DIR_PIPE_HOME + this.getProperty(Configuration.GAZE_DIR) : this.getProperty(Configuration.GAZE_DIR);
        DIR_GAZE_TAGS_PATH = (usePipeHome) ? DIR_PIPE_HOME + this.getProperty(Configuration.GAZE_TAGS) : this.getProperty(Configuration.GAZE_TAGS);
        DIR_GAZE_FEATURES_PATH = (usePipeHome) ? DIR_PIPE_HOME + this.getProperty(Configuration.GAZE_FEATURES) : this.getProperty(Configuration.GAZE_FEATURES);
        DIR_GAZE_LOG_CONF_PATH = (usePipeHome) ? DIR_PIPE_HOME + this.getProperty(Configuration.GAZE_LOG) : this.getProperty(Configuration.GAZE_LOG);

        DIR_CLARK_HOME_PATH = (usePipeHome) ? DIR_PIPE_HOME + this.getProperty(Configuration.CLARK_DIR) : this.getProperty(Configuration.CLARK_DIR);
        DIR_SFST_COMMAND_PATH = (usePipeHome) ? DIR_PIPE_HOME + this.getProperty(Configuration.SFST_COMMAND) : this.getProperty(Configuration.SFST_COMMAND);
        DIR_SFST_TRANSDUCER_HOME_PATH = (usePipeHome) ? DIR_PIPE_HOME + this.getProperty(Configuration.TRANSDUCER_HOME) : this.getProperty(Configuration.TRANSDUCER_HOME);
        DIR_MALT_WORKING_DIR_PATH = (usePipeHome) ? DIR_PIPE_HOME + this.getProperty(Configuration.MALT_WORKING_DIR) : this.getProperty(Configuration.MALT_WORKING_DIR);

        DIR_BASE_URL = null;

        DIR_TOK_EXCEPTIONS_PATH = DIR_PIPE_HOME
                + this.getProperty(Configuration.TOKENIZATION_EXCEPTIONS_LIST);
        DIR_TOK_NO_EOS_ABBR_DICT_PATH = DIR_PIPE_HOME
                + this.getProperty(Configuration.NO_EOS_ABBR_DICT);
        DIR_TOK_POS_EOS_ABBR_DICT_PATH = DIR_PIPE_HOME
                + this.getProperty(Configuration.POSSIBLE_EOS_ABBR_DICT);
        DIR_NAMES_DICT_PATH = DIR_PIPE_HOME
                + this.getProperty(Configuration.NAMES_DICT);
        DIR_CONLL_MAP_PATH = DIR_PIPE_HOME
                + this.getProperty(Configuration.CONLL_MAP_PATH);
        DIR_CONLL_MAP_INV_PATH = DIR_PIPE_HOME
                + this.getProperty(Configuration.CONLL_MAP_INV_PATH);

        if (!new File(DIR_TMP_DIR_PATH).exists()) {
            new File(DIR_PIPE_HOME + DIR_TMP_DIR_PATH).mkdir();
        }

    }

    /**
     * Trims property values
     */
    public void trimProperties() {
        for (Object key : this.keySet()) {
            this.put(key, ((String) this.get(key)).trim());
        }
    }

    /**
     * Gets the CoNLL <code>Map</code>
     * 
     * @return Properties - BTB -> CoNLL <code>Map</code>
     */
    public Properties getConllMap() throws IOException {

        String path = this.getProperty(Configuration.CONLL_MAP_PATH);
        Properties conllMap = new Properties();
        conllMap.load(new FileInputStream(path));
        return conllMap;

    }

    /**
     * Gets pipe home directory path
     * 
     * @return String   - pipe home directory path
     */
    public String getPipeHome() {
        return DIR_PIPE_HOME;
    }

    /**
     * @deprecated since v1.0
     */
    public String getCurrentPath() {
        return DIR_CURRENT_PATH;
    }

    /**
     * Gets Tomcat directory path
     * 
     * @return  String  - Tomcat directory path
     */
    public String getTomcatPath() throws NullPointerException {
        if (DIR_TOMCAT_PATH == null) {
            throw new NullPointerException("Missing 'tomcatdir' entry in 'conf.xml'.");
        }
        return DIR_TOMCAT_PATH;
    }
    
    /**
     * Gets store directory path
     * 
     * @return  String  - project store directory path
     */
    public String getStoreDirPath() throws NullPointerException {
        if (DIR_STORE_DIR_PATH == null) {
            throw new NullPointerException("Missing 'storedir' entry in 'conf.xml'.");
        }
        return DIR_STORE_DIR_PATH;
    }

    /**
     * Gets temp directory path
     * 
     * @return  String  - project temp directory path
     */
    public String getTmpDirPath() throws NullPointerException {
        if (DIR_TMP_DIR_PATH == null) {
            throw new NullPointerException("Missing 'tmpdir' entry in 'conf.xml'.");
        }
        return DIR_TMP_DIR_PATH;
    }

    /**
     * Gets base URL
     * 
     * @return  String  - project base URL
     */
    public String getBaseUrl() throws NullPointerException {
        if (DIR_BASE_URL == null) {
            throw new NullPointerException("Missing 'baseurl' entry in 'conf.xml'.");
        }
        return DIR_BASE_URL;
    }

    /**
     * Gets tokenization exceptions file path
     * 
     * @return  String  - tokenization exceptions file path
     */
    public String getTokExPath() throws NullPointerException {
        if (DIR_TOK_EXCEPTIONS_PATH == null) {
            throw new NullPointerException("Missing 'tokenizationExceptionsList' entry in 'conf.xml'.");
        }
        return DIR_TOK_EXCEPTIONS_PATH;
    }

    /**
     * Gets mid-sentence only abbreviations file path
     * 
     * @return  String  - mid-sentence only abbreviations file path
     */
    public String getTokNoEosDictPath() throws NullPointerException {
        if (DIR_TOK_NO_EOS_ABBR_DICT_PATH == null) {
            throw new NullPointerException("Missing 'noEos' entry in 'conf.xml'.");
        }
        return DIR_TOK_NO_EOS_ABBR_DICT_PATH;
    }

    /**
     * Gets possible end of sentence abbreviations file path
     * 
     * @return  String  - possible end of sentence abbreviations file path
     */
    public String getTokPosEosDictPath() throws NullPointerException {
        if (DIR_TOK_POS_EOS_ABBR_DICT_PATH == null) {
            throw new NullPointerException("Missing 'possibleEos' entry in 'conf.xml'.");
        }
        return DIR_TOK_POS_EOS_ABBR_DICT_PATH;
    }

    /**
     * Gets names dictionary file path
     * 
     * @return  String  - names dictionary file path
     */
    public String getNamesDictPath() throws NullPointerException {
        if (DIR_NAMES_DICT_PATH == null) {
            throw new NullPointerException("Missing 'namesDictionary' entry in 'conf.xml'.");
        }
        return DIR_NAMES_DICT_PATH;
    }

    /**
     * Gets BTB -> CoNLL Map file path
     * 
     * @return  String  - Map file path
     */
    public String getConllMapPath() throws NullPointerException {
        if (DIR_CONLL_MAP_PATH == null) {
            throw new NullPointerException("Missing 'conllMapPath' entry in 'conf.xml'.");
        }
        return DIR_CONLL_MAP_PATH;
    }

    /**
     * Gets CoNLL -> BTB Map file path
     * 
     * @return  String  - Map file path
     */
    public String getConllMapInvPath() throws NullPointerException {
        if (DIR_CONLL_MAP_INV_PATH == null) {
            throw new NullPointerException("Missing 'conllMapInvPath' entry in 'conf.xml'.");
        }
        return DIR_CONLL_MAP_INV_PATH;
    }

    /**
     * Gets Gaze home directory path
     * 
     * @return  String  - Gaze home directory path
     */
    public String getGazeDir() {
        if (DIR_GAZE_DIR == null) {
            throw new NullPointerException("Missing 'gazeDir' entry in 'conf.xml'.");
        }
        return DIR_GAZE_DIR;
    }

    /**
     * Gets Gaze log4j configuration file path
     * 
     * @return  String  - Gaze log4j configuration file path
     */
    public String getGazeConf() {
        if (DIR_GAZE_LOG_CONF_PATH == null) {
            throw new NullPointerException("Missing 'log4config' entry in 'conf.xml'.");
        }
        return DIR_GAZE_LOG_CONF_PATH;
    }

    /**
     * Gets Gaze features file path
     * 
     * @return  String  - Gaze features file path
     */
    public String getGazeFeatures() {
        if (DIR_GAZE_FEATURES_PATH == null) {
            throw new NullPointerException("Missing 'features' entry in 'conf.xml'.");
        }
        return DIR_GAZE_FEATURES_PATH;
    }

    /**
     * Gets Gaze tag file path
     * 
     * @return  String  - Gaze tag file path
     */
    public String getGazeTags() {
        if (DIR_GAZE_TAGS_PATH == null) {
            throw new NullPointerException("Missing 'tags' entry in 'conf.xml'.");
        }
        return DIR_GAZE_TAGS_PATH;
    }

    /**
     * Gets CLaRK home directory path
     * 
     * @return  String  - CLaRK home directory path
     */
    public String getClarkHome() {
        if (DIR_CLARK_HOME_PATH == null) {
            throw new NullPointerException("Missing 'clarkDir' entry in 'conf.xml'.");
        }
        return DIR_CLARK_HOME_PATH;
    }

    /**
     * Gets SFST command (sfst-match) path
     * 
     * @return  String  - SFST command path
     */
    public String getSfstCommand() {
        if (DIR_SFST_COMMAND_PATH == null) {
            throw new NullPointerException("Missing 'sfstCommand' entry in 'conf.xml'.");
        }
        return DIR_SFST_COMMAND_PATH;
    }

    /**
     * Gets transducers home directory path
     * 
     * @return  String   - transducers directory paths
     */
    public String getTransducerHome() {
        if (DIR_SFST_TRANSDUCER_HOME_PATH == null) {
            throw new NullPointerException("Missing 'transducerHome' entry in 'conf.xml'.");
        }
        return DIR_SFST_TRANSDUCER_HOME_PATH;
    }

    /**
     * Gets MaltParser home directory path
     * 
     * @return  String  - MaltParser home directory path
     */
    public String getMaltWorkingDir() {
        if (DIR_MALT_WORKING_DIR_PATH == null) {
            throw new NullPointerException("Missing 'maltWorkingDir' entry in 'conf.xml'.");
        }
        return DIR_MALT_WORKING_DIR_PATH;
    }

    /**
     * Gets default pipe commands
     * 
     * @return  ArrayList&lt;Integer&gt;  - pipe commands
     */
    public ArrayList<Integer> getDefaultPipe() {

        ArrayList<Integer> commands = new ArrayList();

        if (this.containsKey(Configuration.DEFAULT_PIPE)) {
            for (String command : this.getProperty(Configuration.DEFAULT_PIPE).split(";")) {
                commands.add(CommandLineUtils.parseProcessCommand(command));
            }
        } else {
            throw new NullPointerException("Missing 'defaultPipe' entry in 'conf.xml'.");
        }

        return commands;
    }
}
