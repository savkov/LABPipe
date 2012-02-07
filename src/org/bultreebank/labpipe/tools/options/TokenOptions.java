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

/**
 *
 * @deprecated since v1.0
 * @author Aleksandar Savkov
 */
public class TokenOptions {// depricated

    private String TOKEN_DEF;
    private String PUNCT_DEF;
    private String EXCEPTIONS_PATH;

    public TokenOptions() {
    }

    public TokenOptions(String td, String pd, String exceptionsPath) {
        TOKEN_DEF = td;
        PUNCT_DEF = pd;
        EXCEPTIONS_PATH = exceptionsPath;
    }

    public void setTokenDef(String td) {
        TOKEN_DEF = td;
    }

    public void setPunctDef(String pd) {
        PUNCT_DEF = pd;
    }

    public void setExceptionsPath(String ep) {
        EXCEPTIONS_PATH = ep;
    }

    public String getTokenDef() {
        return TOKEN_DEF;
    }

    public String getPunctDef() {
        return PUNCT_DEF;
    }

    public String getExceptionsPath() {
        return EXCEPTIONS_PATH;
    }
    
}
