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
package org.bultreebank.labpipe.tools.options;

import java.util.Set;

/**
 *
 * @deprecated since v1.0
 * @author Aleksandar Savkov
 */
public class SentenceBoundaryOptions {

    private String BOUNDARY_SIGNS;
    private String BOUNDARY_TOKEN;
    private Set<String> NO_EOS;
    private Set<String> POSSIBLE_EOS;
    private Set<String> NAMES_DICTIONARY;
    private int AVERAGE_SENTENCE_LENGTH;

    public SentenceBoundaryOptions() {
    }

    public SentenceBoundaryOptions(String boundaryToken, String signs, Set<String> noEosDict,
            Set<String> possibleEosDict, Set<String> nameDict,
            int avgSentLength) {
        BOUNDARY_SIGNS = signs;
        NO_EOS = noEosDict;
        NAMES_DICTIONARY = nameDict;
        AVERAGE_SENTENCE_LENGTH = avgSentLength;
        BOUNDARY_TOKEN = boundaryToken;
    }
    
    public void setBoundaryToken(String bt) {
        BOUNDARY_TOKEN = bt;
    }

    public void setBoundarySigns(String bs) {
        BOUNDARY_SIGNS = bs;
    }

    public void setNamesDictionary(Set<String> nd) {
        NAMES_DICTIONARY = nd;
    }

    public void setNoEosDict(Set<String> ne) {
        NO_EOS = ne;
    }

    public void setPossibleEosDict(Set<String> pe) {
        POSSIBLE_EOS = pe;
    }

    public void setAverageSentenceLength(int avgSentLength) {
        AVERAGE_SENTENCE_LENGTH = avgSentLength;
    }
    
    public String getBoundaryToken() {
        return BOUNDARY_TOKEN;
    }

    public String getBoundarySigns() {
        return BOUNDARY_SIGNS;
    }

    public Set<String> getDictionary() {
        return NAMES_DICTIONARY;
    }

    public Set<String> getNoEosDict() {
        return NO_EOS;
    }

    public Set<String> getPossibleEosDict() {
        return POSSIBLE_EOS;
    }

    public int getAverageSentenceLength() {
        return AVERAGE_SENTENCE_LENGTH;
    }

}