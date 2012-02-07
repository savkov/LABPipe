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
 * Miscellaneous static methods utilizing different processes in LABPipe.
 *
 * @author Aleksandar Savkov
 */
public class Misc {

    private static final Logger logger = Logger.getLogger(Misc.class.getName());

    /**
     * Inverts a <code>Properties</code> object. Assumes there are no duplicate 
     * values in the key-value pairs. In case of duplicates the last duplicate 
     * in line remains in the resulting object.
     * 
     * @param   props   <code>Properties</code> object
     * 
     * @return  Properties  inverted properties object
     */
    public static Properties invertProperties(Properties props) {

        Properties iProps = new Properties();

        for (Object k : props.keySet()) {

            iProps.put(props.get(k), k);

        }

        return iProps;

    }

    /**
     * Joins a <code>List</code> of Strings with the provided <code>glue</code>
     * 
     * @param   list  List of Strings
     * @param   glue    joining String
     * 
     * @return  String  joint list
     */
    public static String join(List<String> list, String glue) {

        if (list.size() == 1) {
            return list.get(0);
        }

        StringBuilder sb = new StringBuilder();
        for (String token : list) {
            sb.append(token);
            sb.append(glue);
        }

        String result = sb.toString();
        result = result.replaceFirst(glue + "$", "");
        return result;

    }

    /**
     * Joins a <code>String[]</code> with the provided <code>glue</code>
     * 
     * @param   ar  array of Strings
     * @param   glue    joining String
     * 
     * @return  String  joint array
     */
    public static String join(String[] ar, String glue) {

        StringBuilder sb = new StringBuilder();
        for (String token : ar) {
            sb.append(token);
            sb.append(glue);
        }

        String result = sb.toString();

        return result.substring(0, result.length() - glue.length());

    }

    /**
     * Joins two <code>ArrayList</code> objects as columns in a table using <code>colSeparator</code>.
     * 
     * @param   colOne  first column list
     * @param   colTwo  second column list
     * @param   colSeparator    String separating the two columns
     * 
     * @return  String - table
     */
    public static String joinColumns(ArrayList colOne, ArrayList colTwo, String colSeparator) throws IncorrectInputException {
        return joinColumns((String[]) colOne.toArray(new String[0]), (String[]) colTwo.toArray(new String[0]), colSeparator);
    }

    /**
     * Joins two <code>String Array</code> objects as columns in a table using <code>colSeparator</code>.
     * 
     * @param   colOne  first column array
     * @param   colTwo  second column array
     * @param   colSeparator    String separating the two columns
     * 
     * @return  String - table
     */
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

    /**
     * Reads <code>InputStream</code> into a <code>String</code>.
     * 
     * @param   is  read <code>InputStream</code>
     * 
     * @return  String
     */
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

    /**
     * Reads <code>InputStream</code> into a <code>String</code> using the default LABPipe encoding.
     * 
     * @param   is  read <code>InputStream</code>
     * 
     * @return  String
     */
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

    /**
     * Writes an <code>InputStream</code> into an <code>OutputStream</code>.
     * 
     * @param   is  input
     * @param   os output
     */
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
