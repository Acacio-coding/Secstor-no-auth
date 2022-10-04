package com.ifsc.secstor.utils.facade;

import com.at.archistar.crypto.CSSEngine;
import com.at.archistar.crypto.KrawczykEngine;
import com.at.archistar.crypto.PSSEngine;
import com.at.archistar.crypto.ShamirEngine;
import com.at.archistar.crypto.secretsharing.WeakSecurityException;
import com.ifsc.secstor.utils.pvss.SecretShareEngine;
import lombok.Getter;

import java.security.NoSuchAlgorithmException;

@Getter
public class EngineMaker {
    private final Engine shamir;
    private final Engine pss;
    private final Engine css;
    private final Engine krawczyk;
    private final Engine pvss;
    private final int n;
    private final int k;

    public EngineMaker(int n, int k) throws WeakSecurityException, NoSuchAlgorithmException {
        this.shamir = new ArchistarEngine(new ShamirEngine(n, k));
        this.pss = new ArchistarEngine(new PSSEngine(n, k));
        this.css = new ArchistarEngine(new CSSEngine(n, k));
        this.krawczyk = new ArchistarEngine(new KrawczykEngine(n, k));
        this.pvss = new PVSSEngine(new SecretShareEngine(n, k));
        this.n = n;
        this.k = k;
    }

    public void split(String data, String algorithm) throws Exception {
        if (algorithm.equalsIgnoreCase("shamir"))
            this.shamir.split(data);

        if (algorithm.equalsIgnoreCase("pss"))
            this.pss.split(data);

        if (algorithm.equalsIgnoreCase("css"))
            this.css.split(data);

        if (algorithm.equalsIgnoreCase("krawczyk"))
            this.krawczyk.split(data);

        if (algorithm.equalsIgnoreCase("pvss"))
            this.pvss.split(data);

        throw new Exception("Algorithm didn't match any of valid types");
    }

    public String reconstruct(String algorithm) throws Exception {
        if (algorithm.equalsIgnoreCase("shamir") && !this.shamir.getPieces().isEmpty())
            return this.shamir.reconstruct();

        if (algorithm.equalsIgnoreCase("pss") && !this.pss.getPieces().isEmpty())
            return this.pss.reconstruct();

        if (algorithm.equalsIgnoreCase("css") && !this.css.getPieces().isEmpty())
            return this.css.reconstruct();

        if (algorithm.equalsIgnoreCase("krawczyk") && !this.krawczyk.getPieces().isEmpty())
            return this.krawczyk.reconstruct();

        if (algorithm.equalsIgnoreCase("pvss") && !this.pvss.getPieces().isEmpty())
            return this.pvss.reconstruct();

        throw new Exception("You need to split the shares in order to reconstruct them");
    }
}
