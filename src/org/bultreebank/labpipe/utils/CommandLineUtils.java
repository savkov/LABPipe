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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Aleksandar Savkov
 */
public class CommandLineUtils {

    private static final Logger logger = Logger.getLogger(CommandLineUtils.class.getName());
    public static final String OPTIONS_PATH_PAR = "-o";
    public static final String INPUT_PAR = "-in";
    public static final String OUTPUT_PAR = "-out";
    public static final String INPUT_TYPE_PAR = "-itype";
    public static final String OUTPUT_TYPE_PAR = "-otype";
    public static final String PIPE_COMMANDS_PAR = "-p";
    public static final String CONVERTER_PAR = "-c";
    public static final String WORKING_DIR_PAR = "-w";
    public static final String OUTPUT_DIR_PAR = "-wo";
    public static final List<String> PARAMETERS = Arrays.asList(new String[]{OPTIONS_PATH_PAR,
                INPUT_PAR, OUTPUT_PAR, INPUT_TYPE_PAR, OUTPUT_TYPE_PAR,
                PIPE_COMMANDS_PAR, CONVERTER_PAR, WORKING_DIR_PAR, OUTPUT_DIR_PAR});
    public static final int ERROR_INVALID_PARAMETER_MAP = 1;
    public static final int ERROR_IO_NUMBER_MISMATCH = 2;
    public static final int ERROR_INVALID_IO_PARAMETERS = 3;
    public static final int ERROR_INVALID_PARAMETER = 4;
    public static final int ERROR_FILE_NOT_FOUND = 5;
    public static final String TXT_EXT = "txt";
    public static final String LINE_EXT = "line";
    public static final String CTOK_EXT = "ctok";
    public static final String CTAG_EXT = "ctag";
    public static final String CONLL_EXT = "conll";
    public static final String XML_EXT = "xml";
    public static final String DEFAULT_EXT = "out";
    public static final String HELP_MESSEGE = "Usage: java -jar LABPipe.jar <key> <value>,...\n"
            + "> java -jar LABPipe.jar -p mpar -itype line -otype conll "
            + " -o conf/conf.xml -in /home/user/Documents/somefile.line "
            + " -out /home/user/Documents/somefile.conll\n"
            + "List of keys:\n"
            + " -o\tpath to options file. If skipped $HOME/conf/conf.xml will be used\n"
            + " -in\tpath to input file.\n"
            + " -out\tpath to output file.\n"
            + " -itype\tinput type.\n"
            + "\tData types:"
            + "\ttxt, text:\t\t\ttext format\n"
            + "\t\t\tline:\t\t\tline-based entries (tokenized)\n"
            + "\t\t\tgaze:\t\t\tline-based entries (tokenized & tagged), Gaze specific format\n"
            + "\t\t\tctok, ctokens, clark_tokens:\tclark XML-based entries (root->s->tok)\n"
            + "\t\t\tctag, clark_tags:\t\tclark XML-based entries (root->s->tok)\n"
            + "\t\t\tconll:\t\t\t\tCoNLL Data Type\n"
            + "\t\t\twl, weblicht, xml:\t\tWebLicht XML stand-off format\n\n"
            + " -otype	output type.\n"
            + "\tData types:\ttxt, text:\t\t\ttext format\n"
            + "\t\t\tline:\t\t\t\tline-based entries (tokenized)\n"
            + "\t\t\tgaze:\t\t\t\tline-based entries (tokenized & tagged), Gaze specific format\n"
            + "\t\t\tctok, ctokens, clark_tokens:\tclark XML-based entries (root->s->tok)\n"
            + "\t\t\tctag, clark_tags:\t\tclark XML-based entries (root->s->tok)\n"
            + "\t\t\tconll:\t\t\t\tCoNLL Data Type\n"
            + "\t\t\twl, weblicht, xml:\t\tWebLicht XML stand-off format\n"
            + " -p\tpipe commands.\n"
            + "\tCommands:\tstok:\tSFST tokenize\n"
            + "\t\t\trtok:\tRegEx tokenize\n"
            + "\t\t\tctok:\tCLaRK tokenize\n"
            + "\t\t\tstag:\tSVMTool tag\n"
            + "\t\t\tctag:\tCLaRK tag\n"
            + "\t\t\tgtag:\tGaze tag\n"
            + "\t\t\tclem:\tCLaRK lemmatize\n"
            + "\t\t\tccor:\tCLaRK correct (correct POS tags & find lemmas)\n"
            + "\t\t\tmpar:\tMaltParser dependency parse\n"
            + " -c	run as a converter\n"
            + "	-w	working directory. Processes all files in the directory. Collides with -in\n"
            + "	-wo	output directory. Collides with -out\n"
            + "For testing the functionality use:\n"
            + "> java -classpath LABPipe.jar:lib/* org.bultreebank.labpipe.main.Test\n";
    public static final String COPYRIGHT_MESSEGE = "LABPipe  Copyright (C) 2011 Aleksandar Savkov\n"
            + "    This program comes with ABSOLUTELY NO WARRANTY; for details type `show w'.\n"
            + "    This is free software, and you are welcome to redistribute it\n"
            + "    under certain conditions; type `show c' for details.";
    
    public static String[] getFilesInDir(String dirPath) {

        File folder = new File(dirPath);
        File[] listOfFiles = folder.listFiles();
        ArrayList<String> files = new ArrayList();

        for (int i = 0; i < listOfFiles.length; i++) {

            if (listOfFiles[i].isFile()) {
                files.add(dirPath + listOfFiles[i].getName());
            }
        }

        return files.toArray(new String[files.size()]);

    }

    public static String[] generateOutputFileNames(String[] inputFiles, String outputDir, String outputType) {

        String[] outputFiles = new String[inputFiles.length];

        if (outputDir == null) {
            for (int i = 0; i < inputFiles.length; i++) {
                outputFiles[i] = inputFiles[i].replaceFirst("(?<=\\.).{3,5}$", outputType);
            }
        } else {
            if (!outputDir.endsWith(ServiceConstants.SYSTEM_SEPARATOR)) {
                outputDir.concat(ServiceConstants.SYSTEM_SEPARATOR);
            }
            for (int i = 0; i < inputFiles.length; i++) {
                File f = new File(inputFiles[i].replaceFirst("(?<=\\.).{3,5}$", outputType));
                outputFiles[i] = outputDir + f.getName();
            }
        }

        return outputFiles;

    }

    public static HashMap<String, String> makeParameterMap(String[] args) {
        HashMap<String, String> map = new HashMap();

        for (int i = 0; i < args.length; i++) {
            if (PARAMETERS.contains(args[i]) && args[i].startsWith("-")
                    && !args[i + 1].startsWith("-")) {
                map.put(args[i], args[i + 1]);
                i++;
            } else {
                break;
            }
        }

        if (!map.containsKey(INPUT_TYPE_PAR)
                || !map.containsKey(OUTPUT_TYPE_PAR)
                || (!map.containsKey(WORKING_DIR_PAR)
                && (!map.containsKey(INPUT_PAR)
                || !map.containsKey(OUTPUT_PAR)))) {
            throwCommandLineError(ERROR_INVALID_IO_PARAMETERS);
            return null;
        }

        return map;
    }

    public static List<Integer> makeCommandsList(String commands) {

        ArrayList commandsList = new ArrayList();
        for (String c : commands.split(";")) {
            commandsList.add(parseProcessCommand(c));
        }

        return commandsList;

    }

    public static boolean validateParameters(String[] args) {

        if (args.length == 0) {
            System.out.println(HELP_MESSEGE);
        }
        
        if (args.length % 2 == 1) {
            throwCommandLineError(CommandLineUtils.ERROR_INVALID_PARAMETER_MAP);
        }

        for (int i = 0; i < args.length; i++) {
            if (!PARAMETERS.contains(args[i])) {
                throwCommandLineError(ERROR_INVALID_PARAMETER, args[i]);
            }
            if (args[i].startsWith("-")) {
                if (args[i + 1].startsWith("-")) {
                    throwCommandLineError(ERROR_INVALID_PARAMETER_MAP);
                    return false;
                }
                i++;
            }
        }

        return true;
    }

    public static String generateOutputExtension(int type) {

        switch (type) {

            case (ServiceConstants.DATA_TEXT):
                return TXT_EXT;
            case (ServiceConstants.DATA_LINE):
                return LINE_EXT;
            case (ServiceConstants.DATA_CLARK_TOKENS):
                return CTOK_EXT;
            case (ServiceConstants.DATA_CLARK_TAGS):
                return CTAG_EXT;
            case (ServiceConstants.DATA_CONLL):
                return CONLL_EXT;
            case (ServiceConstants.DATA_WEBLICHT):
                return XML_EXT;
            default:
                return DEFAULT_EXT;

        }

    }

    public static int parseDataType(String type) {

        type = type.toLowerCase();
        if (type.equals("txt") || type.equals("text")) {
            return ServiceConstants.DATA_TEXT;
        }
        if (type.equals("line")) {
            return ServiceConstants.DATA_LINE;
        }
        if (type.equals("gaze")) {
            return ServiceConstants.DATA_GAZE;
        }
        if (type.equals("ctag") || type.equals("clark_tags")) {
            return ServiceConstants.DATA_CLARK_TAGS;
        }
        if (type.equals("ctok") || type.equals("ctoken") || type.equals("clarks_tokens")) {
            return ServiceConstants.DATA_CLARK_TOKENS;
        }
        if (type.equals("conll")) {
            return ServiceConstants.DATA_CONLL;
        }
        if (type.equals("wl") || type.equals("xml") || type.endsWith("weblicht")) {
            return ServiceConstants.DATA_WEBLICHT;
        }

        return -1;

    }

    public static int parseProcessCommand(String command) {

        if (command.equals("stok")) {
            return ServiceConstants.PIPE_SFST_TOKENIZE;
        } else if (command.equals("rtok")) {
            return ServiceConstants.PIPE_REGEX_TOKENIZE;
        } else if (command.equals("ctok")) {
            return ServiceConstants.PIPE_CLARK_TOKENIZE;
        } else if (command.equals("stag")) {
            return ServiceConstants.PIPE_SVMTOOL_TAG;
        } else if (command.equals("gtag")) {
            return ServiceConstants.PIPE_GAZE_TAG;
        } else if (command.equals("ctag")) {
            return ServiceConstants.PIPE_CLARK_TAG;
        } else if (command.equals("clem")) {
            return ServiceConstants.PIPE_CLARK_LEMMATIZE;
        } else if (command.equals("ccor")) {
            return ServiceConstants.PIPE_CLARK_CORRECT;
        } else if (command.equals("mpar")) {
            return ServiceConstants.PIPE_MALTPARSER_PARSE;
        }

        return -1;

    }
    
    public static void throwCommandLineError(int errorCode) {
        throwCommandLineError(errorCode, null);
    }

    public static void throwCommandLineError(int errorCode, String par) {
        switch(errorCode) {
            case(CommandLineUtils.ERROR_INVALID_PARAMETER_MAP) : System.out.println("Invalid syntax!"); break;
            case(CommandLineUtils.ERROR_IO_NUMBER_MISMATCH) : System.out.println("Number of input and output files differs."); break;
            case(CommandLineUtils.ERROR_INVALID_IO_PARAMETERS) : System.out.println("Input and Output parameters are not sufficient."); break;
            case(CommandLineUtils.ERROR_INVALID_PARAMETER) : System.out.println("Invalid parameter: " + par); break;
            case(CommandLineUtils.ERROR_FILE_NOT_FOUND) : System.out.println(par); break;
        }
        System.out.println("-------------------------------------------------------");
        System.out.println(HELP_MESSEGE);
    }

    public static boolean diffLines(String file1, String file2) {

        System.out.print("Test file: " + file1 + "\nGold file: " + file2);

        BufferedReader br1 = null;
        BufferedReader br2 = null;
        int diffs = 0;
        int count = 0;
        boolean diff = false;
        try {
            br1 = new BufferedReader(new InputStreamReader(new FileInputStream(file1), "UTF-8"));
            br2 = new BufferedReader(new InputStreamReader(new FileInputStream(file2), "UTF-8"));
            String line1;
            String line2;

            while ((line1 = br1.readLine()) != null && (line2 = br2.readLine()) != null) {
                if (!line1.equals(line2)) {
                    diffs++;
                    System.out.println(line1 + "\n" + line2);
                    diff = true;
                }

                count++;
            }
            if (!diff) {
                System.out.println("\t...identical!\n");
            } else {
                System.out.println("\t...different!\n");
            }

            return diff;

        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Check your sample directory content.", ex);
        } finally {
            try {
                br1.close();
                br2.close();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        return false;

    }
}
