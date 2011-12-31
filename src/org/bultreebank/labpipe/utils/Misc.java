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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.bultreebank.labpipe.exceptions.IncorrectInputException;

/**
 *
 * @author Aleksandar Savkov
 */
public class Misc {

    private static final Logger logger = Logger.getLogger(Misc.class.getName());

    public static Properties invertProperties(Properties props) {

        Properties iProps = new Properties();

        for (Object k : props.keySet()) {

            iProps.put(props.get(k), k);

        }

        return iProps;

    }

    public static String join(List<String> al, String glue) {

        if (al.size() == 1) {
            return al.get(0);
        }

        StringBuilder sb = new StringBuilder();
        for (String token : al) {
            sb.append(token);
            sb.append(glue);
        }

        String result = sb.toString();
        result = result.replaceFirst(glue + "$", "");
        return result;

    }

    public static String join(String[] ar, String glue) {

        StringBuilder sb = new StringBuilder();
        for (String token : ar) {
            sb.append(token);
            sb.append(glue);
        }

        String result = sb.toString();

        return result.substring(0, result.length() - glue.length());

    }

    public static String joinColumns(ArrayList colOne, ArrayList colTwo, String colSeparator) throws IncorrectInputException {
        return joinColumns((String[]) colOne.toArray(new String[0]), (String[]) colTwo.toArray(new String[0]), colSeparator);
    }

    public static String joinColumns(String[] colOne, String[] colTwo, String colSeparator) throws IncorrectInputException {

        StringBuilder sb = new StringBuilder();

        if (colOne.length != colTwo.length) {
            throw new IncorrectInputException("Column lengths differ.");
        }

        for (int i = 0; i < colOne.length; i++) {

            sb.append(colOne[i]);
            sb.append(colSeparator);
            sb.append(colTwo[i]);
            sb.append("\n");

        }

        return sb.toString();

    }

    public static String readInputStream(InputStream is) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        try {
            byte[] bytes = new byte[1024];
            while ((is.read(bytes)) != -1) {
                baos.write(bytes);
                if (is.available() < 1024) {
                    bytes = new byte[is.available()];
                    is.read(bytes);
                    baos.write(bytes);
                    if (is.available() == 0) {
                        break;
                    }
                }
            }

            baos.close();
            return baos.toString(ServiceConstants.PIPE_CHARACTER_ENCODING);

        } catch (IOException ex) {
            logger.fatal("IO Error while reading stream.", ex);
        }
        
        return null;

    }

    public static String readFileInputStream(InputStream is) {

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, ServiceConstants.PIPE_CHARACTER_ENCODING));
            StringBuilder sb = new StringBuilder();

            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");

            }

            is.close();
            return sb.toString();

        } catch (IOException ex) {
            logger.fatal("IO Error while reading stream.", ex);
        }

        return null;

    }

    public static void writeStream(InputStream is, OutputStream os) {

        try {
            byte[] bytes = new byte[1024];
            while ((is.read(bytes) != -1)) {
                os.write(bytes);
                if (is.available() < 1024) {
                    bytes = new byte[is.available()];
                    is.read(bytes);
                    os.write(bytes);
                    if (is.available() == 0) {
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            logger.fatal("IO Error while writing in output stream.", ex);
        }
    }
}
