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
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Aleksandar Savkov
 */

public class EncodingConverter {

    private static final Logger log = Logger.getLogger(EncodingConverter.class.getName());
    private static String newName(String name) {
        return name.replaceAll(".(txt|xml)$", ".utf8.$1");
    }

    public static String convertToUtf8(String filePath, String fromEnc) {
        try {
            File infile = new File(filePath);
            File outfile = new File(newName(filePath));

            Reader in = new InputStreamReader(new FileInputStream(infile), fromEnc);
            Writer out = new OutputStreamWriter(new FileOutputStream(outfile), ServiceConstants.PIPE_CHARACTER_ENCODING);

            int c;

            while ((c = in.read()) != -1) {
                out.write(c);
            }

            in.close();
            out.close();
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Encoding conversion error.", ex);
            return null;
        }

        return newName(filePath);
    }
    private static final Pattern UNICODE_ESCAPE_SEQUENCE = Pattern.compile("#utf([0-9A-F]+);");

    public static String unescapeUnicode(String s) {
        if (s == null || s.equals("")) {
            return s;
        }
        String res = s;
        Matcher m = UNICODE_ESCAPE_SEQUENCE.matcher(res);
        while (m.find()) {
            res = res.replaceAll(m.group(0),
                    Character.toString((char) Integer.parseInt(m.group(1))));
        }
        return res;
    }

    public static String escapeUnicode(String s) {
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (Character.UnicodeBlock.of(ch) == Character.UnicodeBlock.BASIC_LATIN) {
                escaped.append(ch);
            } else {
                // emit entity
                escaped.append("#utf");
                escaped.append((int)ch);
                escaped.append(";");
            }
        }
        return escaped.toString();
    }
}