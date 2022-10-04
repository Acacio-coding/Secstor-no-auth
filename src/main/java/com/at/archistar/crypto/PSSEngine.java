package com.at.archistar.crypto;

import com.at.archistar.crypto.data.*;
import com.at.archistar.crypto.decode.DecoderFactory;
import com.at.archistar.crypto.decode.ErasureDecoderFactory;
import com.at.archistar.crypto.informationchecking.RabinBenOrRSS;
import com.at.archistar.crypto.mac.BCPoly1305MacHelper;
import com.at.archistar.crypto.mac.MacHelper;
import com.at.archistar.crypto.random.BCDigestRandomSource;
import com.at.archistar.crypto.random.RandomSource;
import com.at.archistar.crypto.secretsharing.ReconstructionException;
import com.at.archistar.crypto.secretsharing.ShamirPSS;
import com.at.archistar.crypto.secretsharing.WeakSecurityException;
import com.at.archistar.crypto.data.ReconstructionResult;
import com.at.archistar.crypto.data.Share;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This is a simple CryptoEngine that allows us to use ITS secret-sharing scheme
 * plus Rabin-Ben-Or information checking
 *
 * @author Andreas Happe <andreashappe@snikt.net>
 */
@Component
public class PSSEngine implements CryptoEngine {

    /** our ITS Shamir secret-sharing scheme */
    private final ShamirPSS sharing;

    private final RabinBenOrRSS ic;

    /** how many shares should be generated */
    private final int n;

    /** minimum amount of shares needed to reconstruct original data */
    private final int k;

    /**
     * initialize the crypto engine
     *
     * @param n total number of shares
     * @param k minimum count of shares needed to recreate the original data
     * @throws WeakSecurityException if the k/n selection is insecure
     */
    public PSSEngine(int n, int k) throws NoSuchAlgorithmException, WeakSecurityException {
        this(n, k, new BCDigestRandomSource());
    }

    /**
     * Create a new Shamir Engine.
     *
     * @param n total number of shares
     * @param k minimum count of shares needed to recreate the original data
     * @param rng random number generator to be used
     * @throws WeakSecurityException if the k/n selection is insecure
     */
    PSSEngine(int n, int k, RandomSource rng) throws NoSuchAlgorithmException, WeakSecurityException {
        DecoderFactory decoderFactory = new ErasureDecoderFactory();
        MacHelper mac = new BCPoly1305MacHelper();

        this.sharing = new ShamirPSS(n, k, rng, decoderFactory);
        this.ic = new RabinBenOrRSS(k, mac, rng);
        this.n = n;
        this.k = k;
    }

    @Override
    public Share[] share(byte[] data) {
        PSSShare[] res = new PSSShare[n];
        if (data == null) {
            data = new byte[0];
        }
        byte[][] output = new byte[n][data.length];
        sharing.share(output, data);
        try {
            for (int i = 0; i < n; i++) {
                res[i] = new PSSShare((byte) (i+1), output[i], new HashMap<>(), new HashMap<>());
            }
            ic.createTags(res);
            return res;
        } catch (InvalidParametersException ex) {
            throw new RuntimeException("impossible: share failed: " + ex.getMessage());
        }
    }

    @Override
    public ReconstructionResult reconstruct(Share[] shares) {
        if (!Arrays.stream(shares).allMatch(s -> s instanceof PSSShare)) {
            return new ReconstructionResult(Collections.singletonList("Not all shares are PSS Shares"));
        }
        PSSShare[] pss = Arrays.stream(shares).map(s -> (PSSShare) s).collect(Collectors.toList()).toArray(new PSSShare[shares.length]);
        Map<Boolean, List<InformationCheckingShare>> partitioned = ic.checkShares(pss);
        InformationCheckingShare[] valid = partitioned.get(Boolean.TRUE).toArray(new InformationCheckingShare[partitioned.get(Boolean.TRUE).size()]);
        List<String> errors = partitioned.get(Boolean.FALSE).stream()
                .map(s -> "Could not validate " + s).collect(Collectors.toList());
        try {
            return new ReconstructionResult(sharing.reconstruct(valid), errors);
        } catch (ReconstructionException e) {
            errors.add(e.getMessage());
            return new ReconstructionResult(errors);
        }
    }

    @Override
    public ReconstructionResult reconstructPartial(Share[] shares, long start) {
        if (!Arrays.stream(shares).allMatch(s -> s instanceof PSSShare)) {
            return new ReconstructionResult(Collections.singletonList("Not all shares are PSS Shares"));
        }
        String warning = "*** WARNING: Partial reconstruction -- no Information Checking is performed";
        System.err.println(warning);
        try {
            return new ReconstructionResult(sharing.reconstructPartial(shares, start),
                    Collections.singletonList(warning));
        } catch (ReconstructionException e) {
            return new ReconstructionResult(Collections.singletonList(e.getMessage()));
        }
    }

    @Override
    public Share[] recover(Share[] shares) throws ReconstructionException {
        ReconstructionResult res = reconstruct(shares);
        if (res.isOkay()) {
            return share(res.getData());
        } else {
            throw new ReconstructionException(res.getErrors().stream().reduce((s1, s2) -> s1 + "\n" + s2).orElse(""));
        }
    }

    @Override
    public String toString() {
        return "pss";
    }
}
