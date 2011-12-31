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
 *
 * @author Aleksandar Savkov
 */
public class Configuration extends Properties {

    private static final Logger logger = Logger.getLogger(Configuration.class.getName());
    public static final String PIPE_HOME = "pipeHome";
    public static final String USE_PIPE_HOME = "usePipeHome";
    public static final String PROPERTIES_SOURCE = "propertiesSource";
    public static final String PROPERTIES_SOURCE_FS = "fs";
    public static final String PROPERTIES_SOURCE_SRV = "srv";
    public static final String DEFAULT_PIPE = "defaultPipe";
    public static final String BASE_URL = "baseUrl";
    public static final String TOMCAT_DIR = "tomcatDir";
    public static final String PROJECT_DIR = "projectDir";
    public static final String TMP_DIR = "tmpDir";
    public static final String STORE_DIR = "storeDir";
    public static final String TMP_URL = "tmpUrl";
    public static final String TOKEN_PATTERN = "tokenPattern";
    public static final String PUNCT_PATTERN = "punctPattern";
    public static final String TOKENIZATION_EXCEPTIONS_LIST = "tokenizationExceptionsList";
    public static final String SFST_COMMAND = "sfstCommand";
    public static final String TRANSDUCER_HOME = "transducerHome";
    public static final String TRANSDUCER_SCRIPT = "transducerScript";
    public static final String TRANSDUCER_LIST = "transducerList";
    public static final String CLARK_DIR = "clarkDir";
    public static final String CLARK_DTD = "clarkDtd";
    public static final String CLARK_TOK_SENT_QUERY_NAME = "tokAndSentQuery";
    public static final String CLARK_CONSTRAINTS_QUERY_NAME = "constraintsQuery";
    public static final String CLARK_LEMMAS_QUERY_NAME = "lemmaQuery";
    public static final String CLARK_TAGS_QUERY_NAME = "tagQuery";
    public static final String SVMT_URL = "svmtUrl";
    public static final String BOUNDARY_SIGNS = "boundarySigns";
    public static final String NO_EOS_ABBR_DICT = "noEos";
    public static final String POSSIBLE_EOS_ABBR_DICT = "possibleEos";
    public static final String NAMES_DICT = "namesDictionary";
    public static final String AVG_SENT_LENGTH = "averageSentenceLength";
    public static final String EOS_TOKEN = "eosToken";
    public static final String CONLL_MAP_PATH = "conllMapPath";
    public static final String CONLL_MAP_INV_PATH = "conllMapInvPath";
    public static final String MALT_PARSER_MODEL = "maltParserModel";
    public static final String MALT_WORKING_DIR = "maltWorkingDir";
    public static final String MALT_PARSING_ALGORITHM = "maltParsingAlgorithm";
    public static final String MALT_ROOT = "maltRoot";
    public static final String MALT_LIBLINEAR_OPTIONS = "maltLiblinearOptions";
    public static final String MALT_FEATURE_MODEL = "maltFeatureModel";
    public static final String MALT_DS_COL = "maltDsCol";
    public static final String MALT_DS_DATA_STRUC = "maltDsDataStruc";
    public static final String MALT_DS_THRESHOLD = "maltDsThreshold";
    public static final String MALT_MARKING_STRATEGY = "maltMarkingStrategy";
    public static final String MAIL_HOST = "mailHost";
    public static final String MAIL_FROM = "mailFrom";
    public static final String GAZE_DIR = "gazeDir";
    public static final String GAZE_ENCODING = "encoding";
    public static final String GAZE_CLASSPATH = "classpath";
    public static final String GAZE_XMX = "Xmx";
    public static final String GAZE_XMS = "Xms";
    public static final String GAZE_CLASS = "class";
    public static final String GAZE_ACTION = "action";
    public static final String GAZE_LOG = "log4jconfig";
    public static final String GAZE_FEATURES = "features";
    public static final String GAZE_TAGS = "tags";
    public static final int XML = 1;
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

    public Configuration() {
    }

    public Configuration(String path, int fileType) {

        this.loadConfigFileFromServer(path, fileType);

    }

    @Override
    public String getProperty(String key) {

        return EncodingConverter.unescapeUnicode(super.getProperty(key));

    }

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

    public void trimProperties() {
        for (Object key : this.keySet()) {
            this.put(key, ((String) this.get(key)).trim());
        }
    }

    public Properties getConllMap() throws IOException {

        String path = this.getProperty(Configuration.CONLL_MAP_PATH);
        Properties conllMap = new Properties();
        conllMap.load(new FileInputStream(path));
        return conllMap;

    }

    public String getPipeHome() {
        return DIR_PIPE_HOME;
    }

    public String getCurrentPath() {
        return DIR_CURRENT_PATH;
    }

    public String getTomcatPath() throws NullPointerException {
        if (DIR_TOMCAT_PATH == null) {
            throw new NullPointerException("Missing 'tomcatdir' entry in 'conf.xml'.");
        }
        return DIR_TOMCAT_PATH;
    }

    public String getStoreDirPath() throws NullPointerException {
        if (DIR_STORE_DIR_PATH == null) {
            throw new NullPointerException("Missing 'storedir' entry in 'conf.xml'.");
        }
        return DIR_STORE_DIR_PATH;
    }

    public String getTmpDirPath() throws NullPointerException {
        if (DIR_TMP_DIR_PATH == null) {
            throw new NullPointerException("Missing 'tmpdir' entry in 'conf.xml'.");
        }
        return DIR_TMP_DIR_PATH;
    }

    public String getBaseUrl() throws NullPointerException {
        if (DIR_BASE_URL == null) {
            throw new NullPointerException("Missing 'baseurl' entry in 'conf.xml'.");
        }
        return DIR_BASE_URL;
    }

    public String getTokExPath() throws NullPointerException {
        if (DIR_TOK_EXCEPTIONS_PATH == null) {
            throw new NullPointerException("Missing 'tokenizationExceptionsList' entry in 'conf.xml'.");
        }
        return DIR_TOK_EXCEPTIONS_PATH;
    }

    public String getTokNoEosDictPath() throws NullPointerException {
        if (DIR_TOK_NO_EOS_ABBR_DICT_PATH == null) {
            throw new NullPointerException("Missing 'noEos' entry in 'conf.xml'.");
        }
        return DIR_TOK_NO_EOS_ABBR_DICT_PATH;
    }

    public String getTokPosEosDictPath() throws NullPointerException {
        if (DIR_TOK_POS_EOS_ABBR_DICT_PATH == null) {
            throw new NullPointerException("Missing 'possibleEos' entry in 'conf.xml'.");
        }
        return DIR_TOK_POS_EOS_ABBR_DICT_PATH;
    }

    public String getNamesDictPath() throws NullPointerException {
        if (DIR_NAMES_DICT_PATH == null) {
            throw new NullPointerException("Missing 'namesDictionary' entry in 'conf.xml'.");
        }
        return DIR_NAMES_DICT_PATH;
    }

    public String getConllMapPath() throws NullPointerException {
        if (DIR_CONLL_MAP_PATH == null) {
            throw new NullPointerException("Missing 'conllMapPath' entry in 'conf.xml'.");
        }
        return DIR_CONLL_MAP_PATH;
    }

    public String getConllMapInvPath() throws NullPointerException {
        if (DIR_CONLL_MAP_INV_PATH == null) {
            throw new NullPointerException("Missing 'conllMapInvPath' entry in 'conf.xml'.");
        }
        return DIR_CONLL_MAP_INV_PATH;
    }

    public String getGazeDir() {
        if (DIR_GAZE_DIR == null) {
            throw new NullPointerException("Missing 'gazeDir' entry in 'conf.xml'.");
        }
        return DIR_GAZE_DIR;
    }

    public String getGazeConf() {
        if (DIR_GAZE_LOG_CONF_PATH == null) {
            throw new NullPointerException("Missing 'log4config' entry in 'conf.xml'.");
        }
        return DIR_GAZE_LOG_CONF_PATH;
    }

    public String getGazeFeatures() {
        if (DIR_GAZE_FEATURES_PATH == null) {
            throw new NullPointerException("Missing 'features' entry in 'conf.xml'.");
        }
        return DIR_GAZE_FEATURES_PATH;
    }

    public String getGazeTags() {
        if (DIR_GAZE_TAGS_PATH == null) {
            throw new NullPointerException("Missing 'tags' entry in 'conf.xml'.");
        }
        return DIR_GAZE_TAGS_PATH;
    }

    public String getClarkHome() {
        if (DIR_CLARK_HOME_PATH == null) {
            throw new NullPointerException("Missing 'clarkDir' entry in 'conf.xml'.");
        }
        return DIR_CLARK_HOME_PATH;
    }

    public String getSfstCommand() {
        if (DIR_SFST_COMMAND_PATH == null) {
            throw new NullPointerException("Missing 'sfstCommand' entry in 'conf.xml'.");
        }
        return DIR_SFST_COMMAND_PATH;
    }

    public String getTransducerHome() {
        if (DIR_SFST_TRANSDUCER_HOME_PATH == null) {
            throw new NullPointerException("Missing 'transducerHome' entry in 'conf.xml'.");
        }
        return DIR_SFST_TRANSDUCER_HOME_PATH;
    }

    public String getMaltWorkingDir() {
        if (DIR_MALT_WORKING_DIR_PATH == null) {
            throw new NullPointerException("Missing 'maltWorkingDir' entry in 'conf.xml'.");
        }
        return DIR_MALT_WORKING_DIR_PATH;
    }

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
