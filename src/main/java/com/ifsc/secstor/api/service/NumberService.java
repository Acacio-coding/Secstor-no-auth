package com.ifsc.secstor.api.service;

import com.ifsc.secstor.api.model.NumberModel;

public interface NumberService {
    NumberModel getNumbers();

    void saveNumber(String groupPrimeOrder, String g1, String g2, String secret);

    boolean isEmpty();
}
