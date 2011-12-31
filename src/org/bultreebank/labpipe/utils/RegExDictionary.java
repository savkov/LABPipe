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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Aleksandar Savkov
 */
public class RegExDictionary {

    private static String EXCEPTIONS;
    private Configuration OPTIONS;
    private final Logger logger = Logger.getLogger(RegExDictionary.class.getName());

    public RegExDictionary(Configuration options) throws IOException {
        
        OPTIONS = options;

        String regexDictPath = OPTIONS.getStoreDirPath() + OPTIONS.getProperty(Configuration.TOKENIZATION_EXCEPTIONS_LIST);
        EXCEPTIONS = readInDictionary(regexDictPath);

    }

    private String readInDictionary(String fileName) {
        ArrayList<String> dict = new ArrayList();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), ServiceConstants.PIPE_CHARACTER_ENCODING));
            String line;
            while ((line = br.readLine()) != null) {
                dict.add(line);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return buildRegExDictionary(dict);
    }

    private String buildRegExDictionary(ArrayList<String> dict) {
        StringBuilder re = new StringBuilder();
        re.append("(?:");
        for (String w : dict) {
            re.append(w.replaceAll("([\\.|()$\\^*+?{}\\[\\[-])", "\\\\$1")).append("|");
        }
        re.deleteCharAt(re.length() - 1);
        re.append(")");
        return re.toString();
    }

    public void setRegExDictionary(String dict) {
        EXCEPTIONS = dict;
    }

    public String getRegExDictionary() {
        return EXCEPTIONS;
    }
}
