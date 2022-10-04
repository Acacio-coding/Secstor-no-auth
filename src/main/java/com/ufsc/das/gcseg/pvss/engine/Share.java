/*
 * Share.java
 *
 * Created on 28 de Junho de 2005, 15:36
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.ufsc.das.gcseg.pvss.engine;

import com.ufsc.das.gcseg.pvss.exception.InvalidVSSScheme;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.math.BigInteger;

/**
 *
 * @author neves
 */
@Getter
@Setter
public class Share implements Externalizable {

	private int index;
	private BigInteger encryptedShare;
	private BigInteger share;
	private BigInteger proofc;
	private BigInteger proofr;
	private byte[] U; // for general secret sharing

	public Share() {
	}

	/** Creates a new instance of Share */
	public Share(int index, BigInteger encryptedShare, BigInteger share, BigInteger proofc, BigInteger proofr,
			byte[] U) {
		this.index = index;
		this.encryptedShare = encryptedShare;
		this.share = share;
		this.proofc = proofc;
		this.proofr = proofr;

		this.U = U;
	}

	public void writeExternal(ObjectOutput out) throws IOException {

		byte[] b = encryptedShare.toByteArray();

		out.writeInt(index);
		out.writeInt(b.length);
		out.write(b);

		b = share.toByteArray();
		out.writeInt(b.length);
		out.write(b);

		b = proofc.toByteArray();
		out.writeInt(b.length);
		out.write(b);

		b = proofr.toByteArray();
		out.writeInt(b.length);
		out.write(b);

		out.writeInt(U.length);
		out.write(U);
	}

	public void readExternal(ObjectInput in) throws IOException {
		index = in.readInt();

		int l = in.readInt();
		byte[] b = new byte[l];

		in.readFully(b);
		encryptedShare = new BigInteger(b);

		l = in.readInt();
		b = new byte[l];
		in.readFully(b);
		share = new BigInteger(b);

		l = in.readInt();
		b = new byte[l];
		in.readFully(b);
		proofc = new BigInteger(b);

		l = in.readInt();
		b = new byte[l];
		in.readFully(b);
		proofr = new BigInteger(b);

		l = in.readInt();
		U = new byte[l];
		in.readFully(U);
	}

	public boolean verify(PublicInfoPVSS info, BigInteger publicKey) throws InvalidVSSScheme {
		BigInteger q = info.getGroupPrimeOrder();

		BigInteger a1 = info.getGeneratorG().modPow(proofr, q).multiply(publicKey.modPow(proofc, q)).mod(q);
		BigInteger a2 = share.modPow(proofr, q).multiply(encryptedShare.modPow(proofc, q)).mod(q);

		ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);

		try {
			baos.write(publicKey.toByteArray());
			baos.write(encryptedShare.toByteArray());
			baos.write(a1.toByteArray());
			baos.write(a2.toByteArray());
		} catch (IOException ioe) {
			throw new InvalidVSSScheme("Problems creating hash for proof");
		}

		BigInteger h = PVSSEngine.hash(info, baos.toByteArray()).mod(q);

		return h.equals(proofc);
	}

	public String toString() {
		return "secret(" + index + ")=" + share + ", proof=(" + proofc + "," + proofr + ")";
	}
}
