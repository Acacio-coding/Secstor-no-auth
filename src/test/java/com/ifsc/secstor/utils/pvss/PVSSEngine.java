/*
 * VSSEngine.java
 *
 * Created on 28 de Junho de 2005, 13:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.ifsc.secstor.utils.pvss;

import lombok.Getter;
import lombok.Setter;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author neves
 */
@Getter
@Setter
public class PVSSEngine {

	private final SecureRandom random;
	private final PublicInfoPVSS publicInfo;

	public PVSSEngine(PublicInfoPVSS publicInfo) {
		this.publicInfo = publicInfo;
		this.random = new SecureRandom();
	}


	public PublicInfoPVSS getPublicInfo() {
		return publicInfo;
	}

	public BigInteger generateSecret() {
		return new BigInteger(getPublicInfo().getNumBits() - 1, random);
	}

	public BigInteger[] generateSecretKeys() {
		BigInteger[] secretKeys = new BigInteger[getPublicInfo().getN()];

		for (int i = 0; i < secretKeys.length; i++) {
			secretKeys[i] = generateRandomNumber();

			for (int j = 0; j < i; j++) {
				if (secretKeys[i].equals(secretKeys[j])) {
					i--;
				}
			}
		}

		return secretKeys;
	}


	private BigInteger generateRandomNumber() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		int lineNumber = ThreadLocalRandom.current().nextInt(5000);

		BigInteger toReturn = null;

		try {
			toReturn = new BigInteger(
					Files.readAllLines(Path.of("./timing-tests/utils/NumbersWith511Bits.txt")).get(lineNumber));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return toReturn;
	}

	public BigInteger generatePublicKey(BigInteger secretKey) {
		return getPublicInfo().getGeneratorG().modPow(secretKey, getPublicInfo().getGroupPrimeOrder());
	}

	public PublishedShares generalPublishShares(byte[] data, BigInteger[] publicKeys) throws InvalidVSSScheme {
		BigInteger secret;
		byte[] ensecret;

		do {
			secret = generateSecret();

			BigInteger encryptedSecret = getPublicInfo().getGeneratorG().modPow(secret,
					getPublicInfo().getGroupPrimeOrder());
					
			ensecret = encryptedSecret.toByteArray();
		} while (ensecret.length < 24);

		byte[] U = encrypt(getPublicInfo(), ensecret, data);
		return publishShares(secret, U, publicKeys);
	}

	public PublishedShares publishShares(BigInteger secret, byte[] U, BigInteger[] publicKeys) throws InvalidVSSScheme {
		int t = getPublicInfo().getT();
		int n = getPublicInfo().getN();
		BigInteger g = getPublicInfo().getGeneratorg();

		BigInteger q = getPublicInfo().getGroupPrimeOrder();
		BigInteger qm1 = q.subtract(BigInteger.ONE);

		BigInteger[] coefs = new BigInteger[t];
		BigInteger[] commitments = new BigInteger[t];

		coefs[0] = secret;
		coefs[1] = BigInteger.valueOf(3);

		for (int j = 0; j < t; j++) {
			if (j != 0) {
				coefs[j] = new BigInteger(getPublicInfo().getNumBits() - 1, random);
			}

			commitments[j] = g.modPow(coefs[j], q);
		}

		BigInteger[] shares = new BigInteger[n];
		BigInteger[] encriptedShares = new BigInteger[n];

		BigInteger[] X = new BigInteger[n];
		BigInteger[] a1 = new BigInteger[n];
		BigInteger[] a2 = new BigInteger[n];
		BigInteger[] proofsr = new BigInteger[n];

		ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);

		BigInteger w = BigInteger.valueOf(11);

		for (int i = 0; i < n; i++) {
			shares[i] = poly(coefs, i + 1);
			encriptedShares[i] = publicKeys[i].modPow(shares[i], q);

			// calcs Xi
			BigInteger exp = BigInteger.ONE;
			BigInteger mult = commitments[0];
			for (int j = 1; j < t; j++) {
				exp = exp.multiply(BigInteger.valueOf(i + 1)).mod(qm1);
				mult = mult.multiply(commitments[j].modPow(exp, q)).mod(q);
			}
			X[i] = mult;

			a1[i] = g.modPow(w, q);
			a2[i] = publicKeys[i].modPow(w, q);

			try {
				baos.write(X[i].toByteArray());
				baos.write(encriptedShares[i].toByteArray());
				baos.write(a1[i].toByteArray());
				baos.write(a2[i].toByteArray());
			} catch (IOException ioe) {
				throw new InvalidVSSScheme("Problems creating hash for proof");
			}
		}

		BigInteger proofc = PVSSEngine.hash(getPublicInfo(), baos.toByteArray()).mod(qm1);

		for (int i = 0; i < n; i++) {
			proofsr[i] = w.subtract(shares[i].multiply(proofc)).mod(qm1);
		}

		return new PublishedShares(commitments, encriptedShares, proofsr, proofc, U);
	}

	public byte[] generalCombineShares(Share[] shares) throws InvalidVSSScheme {
		int[] x = new int[shares.length];

		format(shares);

		for (int i = 0; i < shares.length; i++) {
			if (shares[i] != null) {
				x[i] = shares[i].getIndex();
			}
		}

		BigInteger encryptedSecret = combineShares(x, shares);

		return decrypt(getPublicInfo(), encryptedSecret.toByteArray(), shares[0].getU());
	}

	public BigInteger combineShares(int[] x, Share[] shares) {
		if (x.length != shares.length) {
			throw new RuntimeException("There must be " + publicInfo.getT() + " diferent and valid shares");
		}

		for (int i = 0; i < x.length; i++) {
			if (shares[i] == null) {
				throw new RuntimeException("There must be " + publicInfo.getT() + " diferent and valid shares");
			}
		}

		BigInteger q = publicInfo.getGroupPrimeOrder();

		BigInteger secret = BigInteger.ONE;

		for (int i = 0; i < x.length; i++) {
			float lambda = 1;

			for (int j = 0; j < x.length; j++) { // iterates over x[j]
				if (j != i) {
					lambda = lambda * ((float) (x[j] + 1) / (float) (x[j] - x[i]));
				}
			}

			secret = secret.multiply(shares[i].getShare().modPow(BigInteger.valueOf((long) lambda), q)).mod(q);
		}

		return secret;
	}

	private BigInteger poly(BigInteger[] coefs, int val) {
		BigInteger y = coefs[0];

		for (int j = 1; j < coefs.length; j++) {
			BigInteger term = coefs[j].multiply(BigInteger.valueOf((long) Math.pow(val, j)));
			y = y.add(term);
		}

		// poly mod (q-1)
		return y.mod(getPublicInfo().getGroupPrimeOrder().subtract(BigInteger.ONE));
	}

	private void format(Share[] shares) {
		int t = getPublicInfo().getT();
		int last = -2;
		int seq = 0;

		for (int i = 0; i < shares.length; i++) {
			if (shares[i] != null) {
				if (last + 1 == i) {
					seq++;
					if (seq >= t) {
						return;
					}
				} else {
					seq = 1;
					for (int j = 0; j < i; j++) {
						shares[j] = null;
					}
				}
				last = i;
			}
		}
	}

	/////////////////////// some static utilities methods //////////////////////

	private static MessageDigest md = null;

	public static BigInteger hash(PublicInfoPVSS info, byte[] data) throws InvalidVSSScheme {
		try {
			if (md == null) {
				md = MessageDigest.getInstance(info.getHashAlgorithm());
			} else {
				md.reset();
			}

			return new BigInteger(md.digest(data));
		} catch (NoSuchAlgorithmException e) {
			throw new InvalidVSSScheme("Invalid hash algorithm " + info.getHashAlgorithm());
		}
	}

	public static byte[] encrypt(PublicInfoPVSS info, byte[] key, byte[] data) throws InvalidVSSScheme {
		try {
			SecretKey k = SecretKeyFactory.getInstance("DESEDE").generateSecret(new DESedeKeySpec(key));
			Cipher cipher = Cipher.getInstance("DESEDE");
			cipher.init(Cipher.ENCRYPT_MODE, k);

			return cipher.doFinal(data);
		} catch (NoSuchAlgorithmException ex) {
			throw new InvalidVSSScheme("Invalid block cipher algorithm " + info.getHashAlgorithm());
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public static byte[] decrypt(PublicInfoPVSS info, byte[] key, byte[] data) throws InvalidVSSScheme {
		try {
			SecretKey k = SecretKeyFactory.getInstance("DESEDE").generateSecret(new DESedeKeySpec(key));
			Cipher cipher = Cipher.getInstance("DESEDE");
			cipher.init(Cipher.DECRYPT_MODE, k);

			return cipher.doFinal(data);
		} catch (NoSuchAlgorithmException ex) {
			throw new InvalidVSSScheme("Invalid block cipher algorithm " + info.getHashAlgorithm());
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage() + " It also can be because the indexes were not in order, " +
					"using doYourBest parameter can remove one of the keys due to validation error.");
		}
	}
}
