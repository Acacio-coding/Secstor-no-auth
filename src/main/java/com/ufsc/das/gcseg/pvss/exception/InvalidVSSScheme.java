/*
 * InvalidVSSScheme.java
 *
 * Created on 28 de Junho de 2005, 13:34
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.ufsc.das.gcseg.pvss.exception;

import java.io.Serial;

/**
 *
 * @author neves
 */
public class InvalidVSSScheme extends Exception {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an instance of <code>InvalidVSSScheme</code> with the
	 * specified detail message.
	 * 
	 * @param msg the detail message.
	 */
	public InvalidVSSScheme(String msg) {
		super(msg);
	}
}
