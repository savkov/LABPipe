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
package org.bultreebank.labpipe.tools;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bultreebank.labpipe.data.WebLicht;
import org.bultreebank.labpipe.exceptions.IncorrectInputException;
import org.bultreebank.labpipe.utils.Configuration;
import org.bultreebank.labpipe.utils.Misc;
import org.bultreebank.labpipe.utils.Piper;
import org.bultreebank.labpipe.utils.ServiceConstants;

/**
 *
 * @author Aleksandar Savkov
 */
public class FstTokenizer extends Tokenizer {

    private String FST_COMMAND = null;
    private String TRANSDUCER_HOME = null;
    private ArrayList<String[]> COMMANDS = null;
    private String EOS_TOKEN = null;
    private static final Logger logger = Logger.getLogger(FstTokenizer.class.getName());

    public FstTokenizer(Configuration conf) {
        FST_COMMAND = conf.getSfstCommand();
        TRANSDUCER_HOME = conf.getTransducerHome();
        EOS_TOKEN = conf.getProperty(Configuration.EOS_TOKEN);
        setTransducerCommands(conf.getProperty(Configuration.TRANSDUCER_LIST));
    }

    public FstTokenizer(ArrayList<String[]> commandsList, String transducerHome, String sfstCommand, String eosToken) {
        COMMANDS = commandsList;
        TRANSDUCER_HOME = transducerHome;
        FST_COMMAND = sfstCommand;
        EOS_TOKEN = eosToken;
    }

    public final void setTransducerCommands(String transducerStr) {
        ArrayList<String> transducerList = new ArrayList(Arrays.asList(transducerStr.split(";")));
        COMMANDS = new ArrayList();
        COMMANDS.add(new String[]{"tr", "'\n\r'", "' '"});
        String sep = "";
        if (!TRANSDUCER_HOME.endsWith("/")) {
            sep = "/";
        }
        for (String tr : transducerList) {
            COMMANDS.add(new String[]{FST_COMMAND, TRANSDUCER_HOME + sep + tr});
        }
    }
    
    @Override
    public String tokenize(String text) throws InterruptedException {
        
        InputStream is = null;
        try {
            is = new ByteArrayInputStream((text + "<eof>").getBytes(ServiceConstants.PIPE_CHARACTER_ENCODING));
            return tokenize(is);
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_UNSUPPORTED_ENCODING, ex);
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_IO, ex);
            }
        }
        
        return null;
        
    }
    
    public String tokenize(InputStream is) throws InterruptedException {
        try {
            ProcessBuilder pb = new ProcessBuilder();
            Process[] proc = new Process[COMMANDS.size()];
            pb.command(COMMANDS.get(0));
            proc[0] = pb.start();
            Piper piper = new Piper(is, proc[0].getOutputStream());
            piper.run();
            
            for (int i = 1; i < COMMANDS.size(); i++) {
                pb.command(COMMANDS.get(i));
                proc[i] = pb.start();
            }
            
            String result = Misc.readInputStream(Piper.pipe(proc));
            result = result.replaceAll("<eof>", "");
            result = result.replaceAll(ServiceConstants.SFST_EOS_TOKEN, EOS_TOKEN);
            result = result.replaceAll("\n+", "\n");
            result = result.replaceFirst("\n+$", "");
            result = result.trim();
            
            String ending = (result.length() > 100) ? result.substring(result.length() - 100) : result;
            
            if (ending.endsWith(EOS_TOKEN)) {
                return result;
            } else if (!ending.matches(".+" + EOS_TOKEN + "[\n ]*$")) {
                if (!ending.endsWith("\n"))
                    result = result.concat("\n");
                result = result.concat(EOS_TOKEN);
                result = result.concat("\n");
            }
            
            return result;
            
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_IO, ex);
        }
        
        return null;
        
    }

    @Override
    public WebLicht tokenize(File file) throws InterruptedException, IncorrectInputException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), ServiceConstants.PIPE_CHARACTER_ENCODING));
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            br.close();
            WebLicht doc = new WebLicht();
            doc.getTextCorpus().getTextOrTokensOrSentences().add(sb.toString());
            tokenize(doc);
            return doc;
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, ServiceConstants.EXCEPTION_FILE_NOT_FOUND.concat(file.getAbsolutePath()), ex);
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

    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {

        Configuration conf = new Configuration();
        conf.loadConfigFileFromFS("/opt/apache-tomcat-7.0.12/webapps/SVMTWebTagger/WEB-INF/conf/conf.xml", Configuration.XML);
        String fstCommand = conf.getProperty(Configuration.SFST_COMMAND);
        String trPath = conf.getProperty(Configuration.TRANSDUCER_HOME);
        
        String[] tr = conf.getProperty(Configuration.TRANSDUCER_LIST).split(";");
//        FileInputStream fis = new FileInputStream(new File("/home/sasho/ataka.txt"));
//        ProcessBuilder pb = new ProcessBuilder();
//        Process[] proc = new Process[tr.length];
//        pb.command(new String[]{fstCommand, trPath + tr[0]});
//        proc[0] = pb.start();
//        Piper piper = new Piper(fis, proc[0].getOutputStream());
//        piper.run();
//        
//        for (int i = 1; i < tr.length; i++) {
//            pb.command(new String[]{fstCommand, trPath + tr[i]});
//            proc[i] = pb.start();
//        }
//        InputStream is = Piper.pipe(proc);
        StringBuilder text = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/home/sasho/ataka.txt"))));
        String line;
        while((line = br.readLine()) != null) {
            text.append(line);
            text.append("\n");
        }
        FstTokenizer fst = new FstTokenizer(conf);
        System.out.println(fst.tokenize(text.toString()));
        
    }
}
