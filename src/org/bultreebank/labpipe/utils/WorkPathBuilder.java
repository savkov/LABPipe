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
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Aleksandar Savkov
 * @deprecated current system date
 */
public class WorkPathBuilder {

    private String PIPE_HOME;
    private String CURRENT_PATH;
    private String TOMCAT_PATH;
    private String PROJECT_PATH;
    private String TMP_DIR_PATH;
    private String STORE_DIR_PATH;
    private String BASE_URL;
    private String TOK_EXCEPTIONS_PATH;
    private String TOK_NO_EOS_ABBR_DICT_PATH;
    private String TOK_POS_EOS_ABBR_DICT_PATH;
    private String NAMES_DICT_PATH;
    private String CONLL_MAP_PATH;
    private String CONLL_MAP_INV_PATH;
    private String GAZE_DIR;
    private String GAZE_LOG_CONF_PATH;
    private String GAZE_TAGS_PATH;
    private String GAZE_FEATURES_PATH;
    private static final Logger logger = Logger.getLogger(WorkPathBuilder.class.getName());

    public WorkPathBuilder(Properties properties) {

        if (properties.getProperty(Configuration.PROPERTIES_SOURCE).equals(Configuration.PROPERTIES_SOURCE_SRV)) {
            
            PIPE_HOME = properties.getProperty(Configuration.PIPE_HOME);
            TOMCAT_PATH = properties.getProperty(Configuration.TOMCAT_DIR);
            PROJECT_PATH = TOMCAT_PATH + properties.getProperty(Configuration.PROJECT_DIR);
            TMP_DIR_PATH = PROJECT_PATH + properties.getProperty(Configuration.TMP_DIR);
            STORE_DIR_PATH = PROJECT_PATH + properties.getProperty(Configuration.STORE_DIR);
            BASE_URL = properties.getProperty(Configuration.BASE_URL);
            
            GAZE_DIR = PROJECT_PATH + properties.getProperty(Configuration.GAZE_DIR);
            GAZE_TAGS_PATH = GAZE_DIR + properties.getProperty(Configuration.GAZE_TAGS);
            GAZE_FEATURES_PATH = GAZE_DIR + properties.getProperty(Configuration.GAZE_FEATURES);
            GAZE_LOG_CONF_PATH = PROJECT_PATH + properties.getProperty(Configuration.GAZE_LOG);
            
            TOK_EXCEPTIONS_PATH = STORE_DIR_PATH
                    + properties.getProperty(Configuration.TOKENIZATION_EXCEPTIONS_LIST);
            TOK_NO_EOS_ABBR_DICT_PATH = STORE_DIR_PATH
                    + properties.getProperty(Configuration.NO_EOS_ABBR_DICT);
            TOK_POS_EOS_ABBR_DICT_PATH = STORE_DIR_PATH
                    + properties.getProperty(Configuration.POSSIBLE_EOS_ABBR_DICT);
            NAMES_DICT_PATH = STORE_DIR_PATH
                    + properties.getProperty(Configuration.NAMES_DICT);
            CONLL_MAP_PATH = STORE_DIR_PATH
                    + properties.getProperty(Configuration.CONLL_MAP_PATH);
            CONLL_MAP_INV_PATH = STORE_DIR_PATH
                    + properties.getProperty(Configuration.CONLL_MAP_INV_PATH);
            if (!new File(TMP_DIR_PATH).exists()) {
                new File(TMP_DIR_PATH).mkdir();
            }

        } else if (properties.getProperty(Configuration.PROPERTIES_SOURCE).equals(Configuration.PROPERTIES_SOURCE_FS)) {
            try {
                PIPE_HOME = (properties.contains(Configuration.PIPE_HOME)) ? properties.getProperty(Configuration.PIPE_HOME) : new File(".").getCanonicalPath() + ServiceConstants.SYSTEM_SEPARATOR;
                PROJECT_PATH = PIPE_HOME;
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Can't write file to the working directory!", ex);
            }
            TMP_DIR_PATH = PROJECT_PATH + properties.getProperty(Configuration.TMP_DIR);
            STORE_DIR_PATH = PROJECT_PATH;
            
            GAZE_DIR = PROJECT_PATH + properties.getProperty(Configuration.GAZE_DIR);
            GAZE_TAGS_PATH = PROJECT_PATH + properties.getProperty(Configuration.GAZE_TAGS);
            GAZE_FEATURES_PATH = PROJECT_PATH + properties.getProperty(Configuration.GAZE_FEATURES);
            GAZE_LOG_CONF_PATH = PROJECT_PATH + properties.getProperty(Configuration.GAZE_LOG);
            
            BASE_URL = null;

            TOK_EXCEPTIONS_PATH = PROJECT_PATH
                    + properties.getProperty(Configuration.TOKENIZATION_EXCEPTIONS_LIST);
            TOK_NO_EOS_ABBR_DICT_PATH = PROJECT_PATH
                    + properties.getProperty(Configuration.NO_EOS_ABBR_DICT);
            TOK_POS_EOS_ABBR_DICT_PATH = PROJECT_PATH
                    + properties.getProperty(Configuration.POSSIBLE_EOS_ABBR_DICT);
            NAMES_DICT_PATH = PROJECT_PATH
                    + properties.getProperty(Configuration.NAMES_DICT);
            CONLL_MAP_PATH = PROJECT_PATH
                    + properties.getProperty(Configuration.CONLL_MAP_PATH);
            CONLL_MAP_INV_PATH = STORE_DIR_PATH
                    + properties.getProperty(Configuration.CONLL_MAP_INV_PATH);

            if (!new File(TMP_DIR_PATH).exists()) {
                new File(TMP_DIR_PATH).mkdir();
            }
            
        }

    }
    
    public String getPipeHome() {
        return PIPE_HOME;
    }

    public String getCurrentPath() {
        return CURRENT_PATH;
    }

    public String getTomcatPath() throws NullPointerException {
        if (TOMCAT_PATH == null) {
            throw new NullPointerException("Missing 'tomcatdir' entry in the 'conf.xml' file.");
        }
        return TOMCAT_PATH;
    }

    public String getStoreDirPath() throws NullPointerException {
        if (STORE_DIR_PATH == null) {
            throw new NullPointerException("Missing 'storedir' entry in the 'conf.xml' file.");
        }
        return STORE_DIR_PATH;
    }

    public String getTmpDirPath() throws NullPointerException {
        if (TMP_DIR_PATH == null) {
            throw new NullPointerException("Missing 'tmpdir' entry in the 'conf.xml' file.");
        }
        return TMP_DIR_PATH;
    }

    public String getBaseUrl() throws NullPointerException {
        if (BASE_URL == null) {
            throw new NullPointerException("Missing 'baseurl' entry in the 'conf.xml' file.");
        }
        return BASE_URL;
    }

    public String getTokExPath() throws NullPointerException {
        if (TOK_EXCEPTIONS_PATH == null) {
            throw new NullPointerException("Missing 'tokenizationExceptionsList' entry in the 'conf.xml' file.");
        }
        return TOK_EXCEPTIONS_PATH;
    }

    public String getTokNoEosDictPath() throws NullPointerException {
        if (TOK_NO_EOS_ABBR_DICT_PATH == null) {
            throw new NullPointerException("Missing 'noEos' entry in the 'conf.xml' file.");
        }
        return TOK_NO_EOS_ABBR_DICT_PATH;
    }

    public String getTokPosEosDictPath() throws NullPointerException {
        if (TOK_POS_EOS_ABBR_DICT_PATH == null) {
            throw new NullPointerException("Missing 'possibleEos' entry in the 'conf.xml' file.");
        }
        return TOK_POS_EOS_ABBR_DICT_PATH;
    }

    public String getNamesDictPath() throws NullPointerException {
        if (NAMES_DICT_PATH == null) {
            throw new NullPointerException("Missing 'namesDictionary' entry in the 'conf.xml' file.");
        }
        return NAMES_DICT_PATH;
    }

    public String getConllMapPath() throws NullPointerException {
        if (CONLL_MAP_PATH == null) {
            throw new NullPointerException("Missing 'conllMapPath' entry in the 'conf.xml' file.");
        }
        return CONLL_MAP_PATH;
    }
    
    public String getConllMapInvPath() throws NullPointerException {
        if (CONLL_MAP_INV_PATH == null) {
            throw new NullPointerException("Missing 'conllMapInvPath' entry in the 'conf.xml' file.");
        }
        return CONLL_MAP_INV_PATH;
    }
    
    public String getGazeDir() {
        if (GAZE_DIR == null) {
            throw new NullPointerException("Missing 'gazeDir' entry in the 'conf.xml' file.");
        }
        return GAZE_DIR;
    }
    
    public String getGazeConf() {
        if (GAZE_LOG_CONF_PATH == null) {
            throw new NullPointerException("Missing 'log4config' entry in the 'conf.xml' file.");
        }
        return GAZE_LOG_CONF_PATH;
    }
    
    public String getGazeFeatures() {
        if (GAZE_FEATURES_PATH == null) {
            throw new NullPointerException("Missing 'features' entry in the 'conf.xml' file.");
        }
        return GAZE_FEATURES_PATH;
    }
    
    public String getGazeTags() {
        if (GAZE_TAGS_PATH == null) {
            throw new NullPointerException("Missing 'tags' entry in the 'conf.xml' file.");
        }
        return GAZE_TAGS_PATH;
    }
    
}
