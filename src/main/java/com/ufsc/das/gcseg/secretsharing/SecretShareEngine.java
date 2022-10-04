package com.ufsc.das.gcseg.secretsharing;

import com.ufsc.das.gcseg.pvss.PVSSSplitCombine;
import com.ufsc.das.gcseg.pvss.exception.InvalidVSSScheme;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.UnsupportedEncodingException;

import static com.ifsc.secstor.api.util.Constants.PVSS;

public record SecretShareEngine(int n, int k) {

	/**
	 * Split the secret into n parts
	 *
	 * @param secret secret string
	 * @return Shares on a SplitedShares object
	 */
	public SplitedShares split(String secret) throws UnsupportedEncodingException, InvalidVSSScheme {
		PVSSSplitCombine psc = new PVSSSplitCombine(n, k);
		return psc.pVSSSplit(secret);
	}

	/**
	 * Combine shares and return the secret
	 *
	 * @param genShares shares to be combined. You need to create a list of shares
	 * @return secret string
	 */
	public String combine(SharestoCombine genShares) throws InvalidVSSScheme {
		PVSSSplitCombine psc = new PVSSSplitCombine(n, k);
		return psc.pVSScombine(genShares);
	}

	@Override
	public String toString() {
		return PVSS.toUpperCase();
	}
}
