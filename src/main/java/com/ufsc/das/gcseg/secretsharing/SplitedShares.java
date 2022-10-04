package com.ufsc.das.gcseg.secretsharing;

import com.ifsc.secstor.api.model.IndexKeyPair;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.ifsc.secstor.api.util.Constants.PVSS;

@Getter
@Setter
public class SplitedShares {
	private List<IndexKeyPair> shareString;
	private BigInteger modulus;
	private String Key;
	private final String algorithm;

	public SplitedShares() {
		super();
		shareString = new ArrayList<>();
		this.algorithm = PVSS.toUpperCase();
	}

	public void addShare(IndexKeyPair share){
		shareString.add(share);
	}
}
