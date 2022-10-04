package com.ifsc.secstor.utils.pvss;


import com.ifsc.secstor.utils.IndexKeyPair;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

public class PVSSSplitCombine {
	
	int n,t;
	
	public PVSSSplitCombine(int n, int t){
		this.n = n;
		this.t = t;
	}
	
	public Shares pVSSSplit(String secret) throws InvalidVSSScheme {
		SecureRandom random = new SecureRandom();

		int lineNumber = random.nextInt(5000);

		BigInteger groupPrimeOrder = BigInteger.ONE;
		BigInteger g1 = BigInteger.ONE;
		BigInteger g2 = BigInteger.ONE;;

		try {
			groupPrimeOrder = new BigInteger(
					Files.readAllLines(Path.of("./timing-tests/utils/NumbersWith512Bits.txt")).get(lineNumber));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			g1 = new BigInteger(
					Files.readAllLines(Path.of("./timing-tests/utils/NumbersWith511Bits.txt")).get(lineNumber));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			g2 = new BigInteger(
					Files.readAllLines(Path.of("./timing-tests/utils/NumbersWith510Bits.txt")).get(lineNumber));
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Generate public info
		PublicInfoPVSS pi = new PublicInfoPVSS(n, t, groupPrimeOrder, g1, g2);

		// Create an instance of the Engine
		PVSSEngine engine = new PVSSEngine(pi);

		// Generate the Secret keys
		BigInteger[] secretKeys = engine.generateSecretKeys();

		// Generate Public Keys
		// TODO Could be the the same way as the Secret Keys
		BigInteger[] publicKeys = new BigInteger[n];

		for (int i = 0; i < n; i++) {
			publicKeys[i] = engine.generatePublicKey(secretKeys[i]);
		}

		// Generate the shares, proves, etc
		PublishedShares publishedShares = engine.generalPublishShares(secret.getBytes(), publicKeys);

		// Store parts of the share
		List<IndexKeyPair> shares = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			shares.add(new IndexKeyPair(i,
					Base64.getEncoder()
							.encodeToString(publishedShares.getShare(i, secretKeys[i], pi, publicKeys)
									.getShare().toByteArray())));
		}

		// Convert the general key to String
		String key = Base64.getEncoder().encodeToString(publishedShares
				.getShare(0, secretKeys[0], pi, publicKeys).getU());

		return new Shares(shares, groupPrimeOrder, key);
	}

	public String pVSScombine(Shares genShares) throws InvalidVSSScheme {
		PublicInfoPVSS pi = new PublicInfoPVSS(n, t, genShares.getModulus(), null, null);

		PVSSEngine engine = new PVSSEngine(pi);

		Share[] shares = new Share[genShares.getShares().size()];

		byte[] key = Base64.getDecoder().decode(genShares.getKey());

		for (int i = 0; i < genShares.getShares().size(); i++) {
			IndexKeyPair current = genShares.getShares().get(i);

			shares[i] = new Share(
					current.index(),
					null,
					new BigInteger(Base64.getDecoder().decode(current.key())),
					null,
					null,
					key);
		}

		byte[] result = engine.generalCombineShares(shares);

		return new String(result);
	}
}
