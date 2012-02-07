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

import java.io.IOException;
import bpos.SLabelLib;
import bpos.SWordLib;
import bpos.bpos;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bultreebank.labpipe.converters.LineConverter;
import org.bultreebank.labpipe.data.WebLicht;
import org.bultreebank.labpipe.exceptions.IncorrectInputException;
import org.bultreebank.labpipe.exceptions.MissingContentException;
import org.bultreebank.labpipe.utils.CommandLineUtils;
import org.bultreebank.labpipe.utils.Configuration;
import org.bultreebank.labpipe.utils.ServiceConstants;

/**
 * <code>GazeTagger</code> is a wrapper around the Gaze project by Georgi 
 * Georgiev. The input of this tagger is line based entries of 
 * <code>token</code>, <code>suggested tag(s)</code> and <code>O</code>.
 *
 * @author Aleksandar Savkov
 */
public class GazeTagger {
    
    private static final Logger logger = Logger.getLogger(GazeTagger.class.getName());

    /**
     * Tags the WebLicht document <code>tokens</code> and adds <code>tags</code>
     * entries in the same document.
     * 
     * @param   doc WebLicht document
     * @param   options LABPipe configuration
     * @throws IncorrectInputException  
     */
    public static void tagWebLicht(WebLicht doc, Configuration options) throws IncorrectInputException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);

            doc.toLines(baos, options.getProperty(Configuration.EOS_TOKEN), true);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            baos = new ByteArrayOutputStream(1024);

            tagStream(bais, System.out, options);

            LineConverter.toWebLicht(new ByteArrayInputStream(baos.toByteArray()), ServiceConstants.GAZE_EOS_TOKEN, doc);
            
        } catch (MissingContentException ex) {
            Logger.getLogger(GazeTagger.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Tags a Gaze data format stream and outputs it in another stream.
     * 
     * @param   is  input stream (Gaze)
     * @param   os  output stream (Line)
     * @param   options LABPipe configuration
     */
    public static void tagStream(InputStream is, OutputStream os, Configuration options) {

        PropertyConfigurator.configure(options.getGazeConf());
        SWordLib.init();
        SLabelLib.init();
        bpos.tagBgWithRules(is, os, options.getGazeFeatures(), options.getGazeTags());

    }
    
    /**
     * Tags a Gaze data format String.
     * 
     * @param   input   input string
     * @param   options LABPipe configuration
     * 
     * @return  String  - Line encoded data
     */
    public static String tagString(String input, Configuration options) {
        ByteArrayInputStream bais = null;
        try {
            bais = new ByteArrayInputStream(input.getBytes(ServiceConstants.PIPE_CHARACTER_ENCODING));
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            tagStream(bais, baos, options);
            return baos.toString(ServiceConstants.PIPE_CHARACTER_ENCODING);
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_UNSUPPORTED_ENCODING, ex);
        } finally {
            try {
                bais.close();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_IO, ex);
            }
        }
        
        return null;
    }

    public static void main(String[] args) throws Exception {

        //String input = "Третата	Mofsd	O\nЮгославия	Npfsi	O\n,	punct	O\nсъздадена	Vpptcv--sfi	O\nпрез	R	O\nмай	Ncmsi;Tm;Vpitz--2s	O\n1992	M	O\nг.	Ncfsi;Ncfpi	O\nкато	Cs;R	O\nфедерация	Ncfsi	O\nна	R;Te	O\nдвете	Mc-pd;Mcfpd;Mcnpd	O\nостанали	Vppicao-p-i	O\nрепублики	Ncfpi	O\n-	punct	O\nСърбия	Npfsi	O\nи	Cp	O\nЧерна	Afsi	O\nгора	Ncfsi	O\n-	punct	O\nбеше	Vxitf-t2s;Vxitf-t3s	O\nпродукт	Ncmsi	O\nна	R	O\nпропагандната	Afsd	O\nкампания	Ncfsi	O\nна	R	O\nСлободан	Npmsi	O\nМилошевич	H-pi;Hfsi;Hmsi	O\n.	punct	O\n##sb\nВъпросите	Ncmpd	O\n,	punct	O\nпредизвикващи	Vpitcar-p-i	O\nопасения	Amsh;Ncnpi;Vpptcv--smh	O\n,	punct	O\nса	Vxitf-r3p	O\nпо-скоро	Ansi;Dt	O\nпрактически	A-pi;Amsi;Dm	O\n.	punct	O\n##sb\n";

        HashMap<String, String> pars = CommandLineUtils.makeParameterMap(args);
        Configuration conf = new Configuration();
        conf.loadConfigFileFromFS(pars.get("-o"), Configuration.XML);

        if (pars.containsKey("-w")) {

            for (String path : CommandLineUtils.getFilesInDir(pars.get("-w"))) {

                InputStream is = new FileInputStream(path);
                OutputStream os = new FileOutputStream(pars.get("-wo") + new File(path).getName().replaceFirst("\\.[^\\.]+$", ".conll"));

                GazeTagger.tagStream(is, os, conf);

            }

        } else {

            InputStream is = new FileInputStream(pars.get("-in"));
            OutputStream os = new FileOutputStream(pars.get("-out"));

            GazeTagger.tagStream(is, os, conf);

        }

    }
}
