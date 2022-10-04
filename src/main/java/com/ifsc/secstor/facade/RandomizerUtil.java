package com.ifsc.secstor.facade;

import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;

public final class RandomizerUtil {

    private RandomizerUtil() {
    }

    public static String randomize(final Object value) {
        StringBuilder toReturn = new StringBuilder();

        for (int i = 0; i < value.toString().length(); i++) {
            char character = value.toString().toCharArray()[i];
            char generated = generateChar();

            if (Character.isLetter(character)) {
                while (!Character.isLetter(generated) || generated == character) {
                    generated = generateChar();
                }

                if (Character.isUpperCase(character)) {
                    toReturn.append(Character.toUpperCase(generated));
                } else {
                    toReturn.append(generated);
                }

            } else if (Character.isDigit(character)) {
                while (!Character.isDigit(generated) || generated == character) {
                    generated = generateChar();
                }

                toReturn.append(generated);

            } else if (Character.isSpaceChar(character)) {
                toReturn.append(' ');

            } else {
                toReturn.append(character);
            }
        }

        return toReturn.toString();
    }

    private static char generateChar() {
        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('0', 'z')
                .filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS)
                .build();

        return generator.generate(1).toLowerCase().charAt(0);
    }
}
