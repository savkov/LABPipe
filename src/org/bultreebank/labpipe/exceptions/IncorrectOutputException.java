/*
 * LABPipe - Natural Language Processing Pipeline for Bulgarian
 * Copyright (C) 2011 Institute for Information and Communication Technologies 
 *
 * The development of this program was funded by the EuroMatrixPlus Project as 
 * part of the Seventh Framework Program of the European Commission.
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

package org.bultreebank.labpipe.exceptions;

/**
 *
 * @author Aleksandar Savkov
 */
public class IncorrectOutputException extends Exception {

    /**
     * Creates a new instance of <code>IncorrectOutputException</code> without detail message.
     */
    public IncorrectOutputException() {
    }


    /**
     * Constructs an instance of <code>IncorrectOutputException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public IncorrectOutputException(String msg) {
        super(msg);
    }
}
