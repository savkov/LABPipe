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

import de.dspin.data.textcorpus.Dependency;
import de.dspin.data.textcorpus.Depparsing;
import de.dspin.data.textcorpus.ObjectFactory;
import de.dspin.data.textcorpus.Sentence;
import de.dspin.data.textcorpus.Sentences;
import de.dspin.data.textcorpus.TextCorpus;
import de.dspin.data.textcorpus.Token;
import de.dspin.data.textcorpus.TokenRef;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.bultreebank.labpipe.data.Conll;
import org.bultreebank.labpipe.data.WebLicht;
import org.bultreebank.labpipe.utils.ClassMap;
import org.bultreebank.labpipe.utils.Configuration;
import org.bultreebank.labpipe.utils.ServiceConstants;
import org.bultreebank.labpipe.utils.DataUtils;
import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;

/**
 *
 * @author Aleksandar Savkov
 */
public class MaltParserWrapper {

    private static Configuration OPTIONS = null;
    private static ObjectFactory FACTORY = new ObjectFactory();
    private MaltParserService SERVICE = null;
    private static final Logger logger = Logger.getLogger(MaltParserWrapper.class.getName());

    public MaltParserWrapper(Configuration options) throws MaltChainedException {

        OPTIONS = options;
        
        StringBuilder command = new StringBuilder();

        // Read Parsing Model
        command.append("-c ");
        command.append(OPTIONS.getProperty(Configuration.MALT_PARSER_MODEL));

        // Set Work Mode
        command.append(" -m ");
        command.append("parse");

        // Read Working Directory
        command.append(" -w ");
        command.append(OPTIONS.getMaltWorkingDir());

        // Read Parsing Algorithm
        command.append(" -a ");
        command.append(OPTIONS.getProperty(Configuration.MALT_PARSING_ALGORITHM));

        // Read Root
        command.append(" -r ");
        command.append(OPTIONS.getProperty(Configuration.MALT_ROOT));

        // Read Lib-Linear Options
        command.append(" -lo ");
        command.append(OPTIONS.getProperty(Configuration.MALT_LIBLINEAR_OPTIONS));

        // Read Feature Model
        command.append(" -F ");
        command.append(OPTIONS.getProperty(Configuration.MALT_FEATURE_MODEL));

        // Read DSCOL
        command.append(" -d ");
        command.append(OPTIONS.getProperty(Configuration.MALT_DS_COL));

        // Read DS Datastructure
        command.append(" -s ");
        command.append(OPTIONS.getProperty(Configuration.MALT_DS_DATA_STRUC));

        // Read DS Threshold
        command.append(" -T ");
        command.append(OPTIONS.getProperty(Configuration.MALT_DS_THRESHOLD));

        // Read Marking Strategy
        command.append(" -pp ");
        command.append(OPTIONS.getProperty(Configuration.MALT_MARKING_STRATEGY));

        System.out.println("Command attributes     : " + command.toString());

        SERVICE = new MaltParserService();
        SERVICE.initializeParserModel(command.toString());

        System.out.println("Parsing model is loaded!");

    }

    public void parseWebLichtStream(InputStream is, OutputStream os) throws JAXBException, MaltChainedException {
        
        JAXBContext jc = JAXBContext.newInstance("de.dspin.data");
        Marshaller m = jc.createMarshaller();
        WebLicht doc = new WebLicht(is);

        System.out.println("Parsing...");
        parseWebLicht(doc);

        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(doc, os);
        
    }

    public ArrayList<String> parseSentence(ArrayList<String> sentence) throws MaltChainedException {

        ArrayList<String> parsedSentence = null;
        parsedSentence = new ArrayList(
                Arrays.asList(
                SERVICE.parseTokens(
                (String[]) sentence.toArray(new String[0]))));
        return parsedSentence;

    }
    
    public Conll parseConll(Conll conll) throws MaltChainedException {
        Conll parsedConll = new Conll();
        for (ArrayList<String> sentence : conll) {
            parsedConll.add(parseSentence(sentence));
        }
        return parsedConll;
    }
    
    public String parseString(String conll) throws MaltChainedException, UnsupportedEncodingException, IOException {
        
        return parseConll(new Conll(conll)).toString();
        
    }

    public void parseFile(String inFile, String outFile) throws MaltChainedException {

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), ServiceConstants.PIPE_CHARACTER_ENCODING));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), ServiceConstants.PIPE_CHARACTER_ENCODING));
            String line;
            ArrayList<String> sentence = new ArrayList();
            while ((line = br.readLine()) != null) {
                if (line.length() > 0) {
                    sentence.add(line);
                } else {
                    sentence = this.parseSentence(sentence);
                    for (String word : sentence) {
                        bw.write(word);
                        bw.write("\n");
                    }
                    bw.write("\n");
                    sentence = new ArrayList();
                }
            }
            bw.close();
            br.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }


    }

    public void parseWebLicht(WebLicht doc) {

        TextCorpus tc = doc.getTextCorpus();
        List tcl = tc.getTextOrTokensOrSentences();
        Depparsing dp = FACTORY.createDepparsing();
        tcl.add(dp);
        ClassMap cm = new ClassMap(tcl);
        List<Sentence> sentences = ((Sentences) cm.get(Sentences.class)).getSentence();
        String[] conllLine;
        List<TokenRef> tokenRefs = null;
        int index;
        ArrayList<String> conllArray = null;

        for (Sentence sentence : sentences) {
            try {

                tokenRefs = sentence.getTokenRef();
                index = 0;
                Depparsing.Parse parse = FACTORY.createDepparsingParse();
                parse.setID(sentence.getID().replace("sent", "pars"));
                dp.getParse().add(parse);
                conllArray = DataUtils.dspinSentAsConllArray(sentence, cm, OPTIONS);

                for (String parsedWord : this.parseSentence(conllArray)) {

                    parsedWord = parsedWord.replaceAll("\t\t", "\t");
                    conllLine = parsedWord.split("\t");
                    Dependency dependency = FACTORY.createDependency();
                    parse.getDependency().add(dependency);
                    dependency.setFunc(conllLine[7]);
                    dependency.setDepID(tokenRefs.get(index).getTokID());
                    if (Integer.parseInt(conllLine[6]) > 0) {
                        dependency.setGovID(tokenRefs.get(Integer.parseInt(conllLine[6]) - 1).getTokID());
                    }

                    index++;

                }

            } catch (MaltChainedException ex) {
                StringBuilder sb = new StringBuilder();
                sb.append("Problem when parsing sentence number ");
                sb.append(sentence.getID());
                sb.append(": ");
                for (TokenRef word : sentence.getTokenRef()) {
                    sb.append(((Token) word.getTokID()).getValue());
                    sb.append(" (");
                    sb.append(((Token) word.getTokID()).getID());
                    sb.append(" )");
                    sb.append(" ");
                }

                logger.log(Level.SEVERE, sb.toString(), ex);
            }
        }

    }

    public static void parseFileStatic(String inFile, String outFile) {


        StringBuilder command = new StringBuilder();

        // Read Parsing Model
        command.append("-c ");
        command.append(OPTIONS.getProperty("parsermodel"));

        // Set Work Mode
        command.append(" -m ");
        command.append("parse");

        // Read Working Directory
        command.append(" -w ");
        command.append(OPTIONS.getProperty("workingdir"));

        // Set Input File
        command.append(" -i ");
        command.append(inFile);

        // Set Output File
        command.append(" -o ");
        command.append(outFile);

        // Read Parsing Algorithm
        command.append(" -a ");
        command.append(OPTIONS.getProperty("parsingalgorithm"));

        // Read Root
        command.append(" -r ");
        command.append(OPTIONS.getProperty("root"));

        // Read Lib-Linear Options
        command.append(" -lo ");
        command.append(OPTIONS.getProperty("liblinearoptions"));

        // Read Feature Model
        command.append(" -F ");
        command.append(OPTIONS.getProperty("featuremodel"));

        // Read DSCOL
        command.append(" -d ");
        command.append(OPTIONS.getProperty("dscol"));

        // Read DS Datastructure
        command.append(" -s ");
        command.append(OPTIONS.getProperty("dsdatastruc"));

        // Read DS Threshold
        command.append(" -T ");
        command.append(OPTIONS.getProperty("dsthreshold"));

        // Read Marking Strategy
        command.append(" -pp ");
        command.append(OPTIONS.getProperty("markingstrategy"));

        try {
            // Parses the test data file using the parser model model0.mco and using the option container 0
            new MaltParserService().runExperiment(command.toString());
            // Parses the test data file using the parser model model1.mco and using the option container 1
        } catch (MaltChainedException e) {
            System.err.println("MaltParser exception : " + e.getMessage());
        }

    }
    
}
