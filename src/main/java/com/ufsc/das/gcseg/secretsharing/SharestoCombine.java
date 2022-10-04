package com.ufsc.das.gcseg.secretsharing;

import com.ifsc.secstor.api.model.IndexKeyPair;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SharestoCombine {

	private List<IndexKeyPair> shareString;
	private String Key;
	private BigInteger modulus;

	public SharestoCombine(BigInteger modulus, String key) {
		this.shareString = new ArrayList<>();
		this.modulus = modulus;
		this.Key = key;
	}

    public SharestoCombine() {
		this.shareString = new ArrayList<>();
    }

	/**
	* Add a share to the list
	*
	* @param share the ith share in string
	* 	*/		
	public void addShare(IndexKeyPair share){
		shareString.add(share);
	}
}
