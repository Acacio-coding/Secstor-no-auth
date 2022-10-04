package com.ufsc.das.gcseg.pvss.engine;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;

/*
 * PublicInfo.java
 *
 * Created on 28 de Junho de 2005, 13:25
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

/**
 *
 * @author neves
 */
@Getter
@Setter
public class PublicInfoPVSS implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;
	private final int n;
	private final int t;
	private final int numBits;
	private final BigInteger groupPrimeOrder;
	private final BigInteger generatorg;
	private final BigInteger generatorG;

	/**
	 * Creates a new instance of PublicInfo
	 */
	public PublicInfoPVSS(int n, int t, BigInteger groupPrimeOrder, BigInteger generatorg, BigInteger generatorG) {
		this.n = n;
		this.t = t;
		this.numBits = groupPrimeOrder.bitLength() - 1;
		this.groupPrimeOrder = groupPrimeOrder;
		this.generatorg = generatorg;
		this.generatorG = generatorG;
	}

	public BigInteger getGeneratorg() {
		return generatorg;
	}

	public BigInteger getGeneratorG() {
		return generatorG;
	}

	public String getHashAlgorithm() {
		return "SHA-1";
	}

	public String toString() {
		return "(" + n + "," + t + ")-" + numBits + " bits. q=" + groupPrimeOrder + ", g=" + generatorg + ", G="
				+ generatorG;
	}
}
