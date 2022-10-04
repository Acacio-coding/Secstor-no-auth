package com.ufsc.das.gcseg.pvss;

import com.ifsc.secstor.api.model.IndexKeyPair;
import com.ifsc.secstor.api.model.NumberModel;
import com.ifsc.secstor.api.util.BeanUtil;
import com.ifsc.secstor.api.service.NumberServiceImplementation;
import com.ufsc.das.gcseg.pvss.engine.PVSSEngine;
import com.ufsc.das.gcseg.pvss.engine.PublicInfoPVSS;
import com.ufsc.das.gcseg.pvss.engine.PublishedShares;
import com.ufsc.das.gcseg.pvss.engine.Share;
import com.ufsc.das.gcseg.pvss.exception.InvalidVSSScheme;
import com.ufsc.das.gcseg.secretsharing.SharestoCombine;
import com.ufsc.das.gcseg.secretsharing.SplitedShares;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Base64;

@Getter
@Component
public class PVSSSplitCombine {
	
	private final int n;
	private final int t;

	private final NumberServiceImplementation numberService =
			BeanUtil.getBean(NumberServiceImplementation.class);

	public PVSSSplitCombine(int n, int t) {
		this.n = n;
		this.t = t;
	}

	public SplitedShares pVSSSplit(String secret) throws InvalidVSSScheme {
		// Database implementation
		NumberModel bigIntegerNumbers = numberService.getNumbers();

		BigInteger groupPrimeOrder = new BigInteger(bigIntegerNumbers.getGroupPrimeOrder());
		BigInteger g1 = new BigInteger(bigIntegerNumbers.getG1());
		BigInteger g2 = new BigInteger(bigIntegerNumbers.getG2());

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

		SplitedShares ss = new SplitedShares();
		ss.setModulus(groupPrimeOrder);

		// Store parts of the share
		for (int i = 0; i < n; i++) {
			ss.addShare(new IndexKeyPair(i,
					Base64.getEncoder()
							.encodeToString(publishedShares.getShare(i, secretKeys[i], pi, publicKeys)
									.getShare().toByteArray())));
		}

		// Convert the general key to String
		String key = Base64.getEncoder().encodeToString(publishedShares
				.getShare(0, secretKeys[0], pi, publicKeys).getU());

		ss.setKey(key);

		return ss;
	}

	public String pVSScombine(SharestoCombine genShares) throws InvalidVSSScheme {
		// Create an instance of the Engine
		PublicInfoPVSS pi = new PublicInfoPVSS(n, t, genShares.getModulus(), null, null);

		PVSSEngine engine = new PVSSEngine(pi);

		Share[] shares = new Share[genShares.getShareString().size()];

		byte[] key = Base64.getDecoder().decode(genShares.getKey());

		for (int i = 0; i < genShares.getShareString().size(); i++) {
			IndexKeyPair current = genShares.getShareString().get(i);

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
