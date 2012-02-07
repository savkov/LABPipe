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
package org.bultreebank.labpipe.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bultreebank.labpipe.data.WebLicht;
import org.bultreebank.labpipe.exceptions.IncorrectInputException;
import org.bultreebank.labpipe.utils.Configuration;
import org.bultreebank.labpipe.utils.Misc;
import org.bultreebank.labpipe.utils.RegExDictionary;
import org.bultreebank.labpipe.utils.ServiceConstants;

/**
 *
 * DEPRICATED
 * @deprecated since v1.0
 * @author Aleksandar Savkov
 */
public class RegExTokenizer extends Tokenizer {

    public static final String EXCEPTIONS_PATH = "exception";
    private Configuration OPTIONS;
    private RegExDictionary RED;
    private Pattern TOKEN_PATTERN;
    private static final Logger logger = Logger.getLogger(RegExTokenizer.class.getName());

    /**
     * 
     * @param options
     * @throws IOException
     */
    public RegExTokenizer(Configuration options) throws IOException {
        OPTIONS = options;
        RED = new RegExDictionary(OPTIONS);
        setTokenPattern();
    }

    /**
     * 
     * @param options
     * @param dictPath
     * @throws IOException
     */
    public RegExTokenizer(Configuration options, String dictPath) throws IOException {
        OPTIONS = options;
        RED = new RegExDictionary(OPTIONS);
        setTokenPattern();
    }

    private void setTokenPattern() {
        TOKEN_PATTERN = Pattern.compile("(" + RED.getRegExDictionary() + "+|"
                + OPTIONS.getProperty(Configuration.TOKEN_PATTERN) + "+|"
                + OPTIONS.getProperty(Configuration.PUNCT_PATTERN)
                + ")");
    }

    @Override
    public WebLicht tokenize(File file) throws IncorrectInputException, InterruptedException {
        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), ServiceConstants.PIPE_CHARACTER_ENCODING));
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            br.close();
            WebLicht doc = new WebLicht();
            doc.getTextCorpus().getTextOrTokensOrSentences().add(sb.toString());
            tokenize(doc);
            return doc;
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_FILE_NOT_FOUND, ex);
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_UNSUPPORTED_ENCODING, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_IO, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_IO, ex);
            }
        }

        return null;

    }

    @Override
    public String tokenize(String text) {

        try {
            BufferedReader br = new BufferedReader(new StringReader(text));
            StringBuilder tokText = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                tokText.append(Misc.join(tokenizeLine(line), "\n"));
                tokText.append("\n");
            }
            br.close();

            return tokText.toString().replaceAll("\n+", "\n");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_IO, ex);
        }

        return null;

    }

    private List<String> tokenizeLine(String text) {
        List<String> tokens = new ArrayList();
        Matcher m = TOKEN_PATTERN.matcher(text);
        while (m.find()) {
            tokens.add(m.group(1));
        }
        return tokens;
    }
}
