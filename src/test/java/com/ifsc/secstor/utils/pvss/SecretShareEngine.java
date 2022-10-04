package com.ifsc.secstor.utils.pvss;

import lombok.Getter;

@Getter
public class SecretShareEngine {

	int n, k;

	/**
	* Create an object for the SecretShareEngine 
	*
	* @param  n  number of part to be divided
	* @param  k mininum parts to restore the secret
	* 	*/
	public SecretShareEngine(int n, int k) {
		this.n = n;
		this.k = k;
	}

	/**
	* Split the secret into n parts
	*
	* @param  secret secret string
	* @return Shares on a SplitedShares object
	* 	*/	
	public Shares split(String secret) throws InvalidVSSScheme {
		PVSSSplitCombine psc = new PVSSSplitCombine(n, k);
		return psc.pVSSSplit(secret);
	}

	/**
	* Combine shares and return the secret
	*
	* @param  genShares shares to be combined. You need to create a list of shares
	* @return  secret string
	* 	*/	
	public String combine(Shares genShares) throws InvalidVSSScheme{
		PVSSSplitCombine psc = new PVSSSplitCombine(n, k);
		return psc.pVSScombine(genShares);
	}

	@Override
	public String toString() {
		return "PVSS";
	}
}
