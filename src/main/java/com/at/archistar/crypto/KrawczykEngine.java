package com.at.archistar.crypto;

import com.at.archistar.crypto.data.InvalidParametersException;
import com.at.archistar.crypto.data.ReconstructionResult;
import com.at.archistar.crypto.data.Share;
import com.at.archistar.crypto.decode.DecoderFactory;
import com.at.archistar.crypto.decode.ErasureDecoderFactory;
import com.at.archistar.crypto.random.BCDigestRandomSource;
import com.at.archistar.crypto.random.RandomSource;
import com.at.archistar.crypto.secretsharing.KrawczykCSS;
import com.at.archistar.crypto.secretsharing.ReconstructionException;
import com.at.archistar.crypto.secretsharing.WeakSecurityException;
import com.at.archistar.crypto.symmetric.ChaCha20Encryptor;
import com.at.archistar.crypto.symmetric.Encryptor;
import org.springframework.stereotype.Component;

import java.util.Collections;


@Component
public class KrawczykEngine implements CryptoEngine {

    private final int k;
    private final int n;
    private final KrawczykCSS engine;

    public KrawczykEngine(int n, int k) throws WeakSecurityException {
        this(n, k, new BCDigestRandomSource());
    }

    public KrawczykEngine(int n, int k, RandomSource rng) throws WeakSecurityException {
        this.n = n;
        this.k = k;
        DecoderFactory decoderFactory = new ErasureDecoderFactory();
        Encryptor cryptor = new ChaCha20Encryptor();
        engine = new KrawczykCSS(n, k, rng, cryptor, decoderFactory);
    }

    public KrawczykEngine(int n, int k, RandomSource rng, byte[] additionalKey) throws InvalidParametersException, WeakSecurityException {
        this.n = n;
        this.k = k;
        DecoderFactory decoderFactory = new ErasureDecoderFactory();
        Encryptor cryptor = new ChaCha20Encryptor();
        engine = new KrawczykCSS(n, k, rng, cryptor, decoderFactory, additionalKey);
    }

    @Override
    public Share[] share(byte[] data) {
        return engine.share(data);
    }

    @Override
    public ReconstructionResult reconstruct(Share[] shares) {
        try {
            return new ReconstructionResult(engine.reconstruct(shares));
        } catch (ReconstructionException e) {
            return new ReconstructionResult(Collections.singletonList(e.toString()));
        }
    }

    @Override
    public ReconstructionResult reconstructPartial(Share[] shares, long start) {
        try {
            return new ReconstructionResult(engine.reconstructPartial(shares, start));
        } catch (ReconstructionException e) {
            return new ReconstructionResult(Collections.singletonList(e.toString()));
        }
    }

    @Override
    public Share[] recover(Share[] shares) throws ReconstructionException {
        return engine.recover(shares);
    }

    @Override
    public String toString() {
        return "krawczyk";
    }
}
