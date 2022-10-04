package com.ifsc.secstor.facade;

import com.ifsc.secstor.api.advice.exception.ValidationException;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.http.HttpStatus;

import java.security.SecureRandom;
import java.text.*;
import java.util.*;

import static com.ifsc.secstor.api.advice.messages.ErrorMessages.INVALID_GENERALIZATION_STRING_LENGTH;
import static com.ifsc.secstor.api.advice.messages.ErrorMessages.INVALID_SIMILARITY_LEVEL;
import static com.ifsc.secstor.api.advice.paths.Paths.DATA_ANONYMIZATION_BASE_AND_ANONYMIZE;

public final class GeneralizatorUtil {

    private GeneralizatorUtil() {
    }

    //Util Methods
    public static List<String> receiveData(List<Object> data, String label) {
        List<?> nullSafetyList = getNullSafetyList(data);

        if (nullSafetyList.stream().allMatch(current -> current instanceof Integer)) {
            List<Integer> toGeneralize =  nullSafetyList.stream().map(current -> (Integer) current).toList();
            return generalizeIntegers(label, toGeneralize);
        }

        if (nullSafetyList.stream().allMatch(current -> current instanceof Double)) {
            List<Double> toGeneralize =  nullSafetyList.stream().map(current -> (Double) current).toList();
            return generalizeDoubles(label, toGeneralize);
        }

        List<String> toGeneralize = nullSafetyList.stream().map(Object::toString).toList();

        if (toGeneralize.stream().allMatch(current -> {
            try {
                DateUtils.parseDate(String.valueOf(current), "dd/MM/yyyy", "dd-MM-yyyy");
                return true;
            } catch (ParseException e) {
                return false;
            }
        })) {
            return generalizeDates(label, toGeneralize);
        }

        return generalizeStrings(toGeneralize);
    }

    public static  List<String> receiveDataWithLevel(List<Object> data, String label, int level) {
        getNullSafetyList(data);

        if (data.stream().allMatch(current -> current instanceof Integer)) {
            List<Integer> toGeneralize =  data.stream().map(current -> (Integer) current).toList();
            return generalizeIntegersWithRange(label, toGeneralize, level);
        }

        if (data.stream().allMatch(current -> current instanceof Double)) {
            List<Double> toGeneralize =  data.stream().map(current -> (Double) current).toList();
            return generalizeDoublesWithRange(label, toGeneralize, level);
        }

        List<String> toGeneralize = data.stream().map(String::valueOf).toList();

        if (data.stream().allMatch(current -> {
            try {
                DateUtils.parseDate(String.valueOf(current), "dd/MM/yyyy", "dd-MM-yyyy");
                return true;
            } catch (ParseException e) {
                return false;
            }
        })) {
            return generalizeDatesWithRange(label, toGeneralize, level);
        }

        return generalizeStringsWithLevel(toGeneralize, level);
    }

    private static <T> List<T> getNullSafetyList(final List<T> data) {
        return data.stream().filter(Objects::nonNull).toList();
    }

    private static List<Date> getDateList(final List<String> data) {
        return data.stream().map(current -> {
            try {
                return DateUtils.parseDate(current, "dd/MM/yyyy", "dd-MM-yyyy");
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }).toList();
    }

    private static Integer getRandomRange() {
        final Random random = new SecureRandom();

        return random.nextInt(1, 11);
    }

    private static List<String> getGeneralizedIntegers(String label, List<Integer> nullSafetyNumberRows,
                                                       Integer max, Integer min) {
        final List<String> toReturn = new ArrayList<>();

        for (final Integer number : nullSafetyNumberRows) {
            if (number > min && number <= max ) {
                toReturn.add(min + " < " + label + " <= " + max);
            }

            if (number <= min) {
                toReturn.add(label + " <= " + min);
            }
        }

        return toReturn;
    }

    private static Integer getMaxIntegerNumber(final List<Integer> numbers) {
        Integer max = 0;

        for (final Integer number : numbers) {
            if (number > max) {
                max = number;
            }
        }

        return max;
    }

    private static Integer getMinIntegerNumber(final List<Integer> numbers, final Integer maxNumber) {
        Integer min = maxNumber;

        for (final Integer number : numbers) {
            if (number < min) {
                min = number;
            }
        }

        return min;
    }

    private static List<String> getGeneralizedDoubles(String label, List<Double> nullSafetyNumberRows,
                                                      Double max, Double min) {
        final List<String> toReturn = new ArrayList<>();

        NumberFormat formatter = new DecimalFormat("#0.00",
                DecimalFormatSymbols.getInstance(new Locale("en")));

        for (final Double number : nullSafetyNumberRows) {
            if (number > min && number <= max ) {
                toReturn.add(formatter.format(min) + " < " + label + " <= " + formatter.format(max));
            }

            if (number <= min) {
                toReturn.add(label + " <= " + formatter.format(min));
            }
        }

        return toReturn;
    }

    private static Double getMaxDoubleNumber(final List<Double> numbers) {
        Double max = 0.0;

        for (final Double number : numbers) {
            if (number > max) {
                max = number;
            }
        }

        return max;
    }

    private static Double getMinDoubleNumber(final List<Double> numbers, final Double maxNumber) {
        Double min = maxNumber;

        for (final Double number : numbers) {
            if (number < min) {
                min = number;
            }
        }

        return min;
    }

    private static void validateStringGeneralization(List<String> data) {
        if (!checkIfAllStringsHaveTheSameSize(data)) {
            throw new ValidationException(HttpStatus.BAD_REQUEST, INVALID_GENERALIZATION_STRING_LENGTH,
                    DATA_ANONYMIZATION_BASE_AND_ANONYMIZE);
        }

        if (!checkIfSimilarityLevelIsValid(data)) {
            throw new ValidationException(HttpStatus.BAD_REQUEST, INVALID_SIMILARITY_LEVEL,
                    DATA_ANONYMIZATION_BASE_AND_ANONYMIZE);
        }
    }

    private static Boolean checkIfAllStringsHaveTheSameSize(final List<String> data) {
        final String firstRow = data.get(0);

        for (int i = 1; i < data.size(); i++) {
            if (data.get(i).length() < firstRow.length() ||
                    data.get(i).length() > firstRow.length()) {
                return false;
            }
        }

        return true;
    }

    private static Boolean checkIfSimilarityLevelIsValid(final List<String> data) {
        final String firstRow = data.get(0);

        for (int i = 1; i < data.size(); i++) {
            if (data.get(i).charAt(0) != firstRow.charAt(0)) {
                return false;
            }
        }

        return true;
    }

    private static Boolean checkIfLevelIsValid(final List<String> data, final int level) {
        if (level < 0) {
            return false;
        }

        for (String row : data) {
            if (level > row.length()) {
                return false;
            }
        }

        return true;
    }

    private static Integer getLowestIndexWithoutSimilarity(final List<String> data) {
        final String firstRow = data.get(0);
        int lowestIndexWithoutSimilarity = firstRow.length();

        for (int i = 1; i < data.size(); i++) {
            final String currentRow = data.get(i);

            for (int j = 0; j < currentRow.length(); j++) {
                if (currentRow.charAt(j) != firstRow.charAt(j) &&
                        j < lowestIndexWithoutSimilarity) {
                    lowestIndexWithoutSimilarity = j;
                }
            }
        }

        return lowestIndexWithoutSimilarity;
    }

    private static Date getNewestDate(final List<Date> data) {
        Date newest = new Date(Long.MIN_VALUE);

        for (Date currentDate : data) {
            if (currentDate.after(newest)) {
                newest = currentDate;
            }
        }

        return newest;
    }

    private static Date getOldestDate(final List<Date> data, final Date newest) {
        Date oldest = newest;

        for (Date currentDate : data) {
            if (currentDate.before(oldest)) {
                oldest = currentDate;
            }
        }

        return oldest;
    }

    private static List<String> getGeneralizedDates(final List<Date> data, final Date newest, final Date oldest,
                                                    String label) {
        final List<String> toReturn = new ArrayList<>();

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

        for (final Date currentDate : data) {
            if (currentDate.after(oldest) && currentDate.before(newest) || currentDate.equals(newest)) {
                toReturn.add(formatter.format(oldest) + " < " + label + " <= " + formatter.format(newest));
            }

            if (currentDate.before(oldest) || currentDate.equals(oldest)) {
                toReturn.add(label + " <= " + formatter.format(oldest));
            }
        }

        return toReturn;
    }

    private static List<String> getDateResults(String label, int range, List<Date> dateList) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getNewestDate(dateList));
        calendar.add(Calendar.DATE, range);

        final Date newest = calendar.getTime();

        calendar.setTime(getOldestDate(dateList, newest));
        calendar.add(Calendar.DATE, range);

        final Date oldest = calendar.getTime();

        return getGeneralizedDates(dateList, newest, oldest, label);
    }


    // Generalize Integers
    public static List<String> generalizeIntegers(final String label, final List<Integer> data) {
        final List<Integer> nullSafetyNumberRows = getNullSafetyList(data);

        final Integer range = getRandomRange();

        final Integer max = getMaxIntegerNumber(nullSafetyNumberRows) + range;
        final Integer min = getMinIntegerNumber(nullSafetyNumberRows, max) + range;

        return getGeneralizedIntegers(label, nullSafetyNumberRows, max, min);
    }

    public static List<String> generalizeIntegersWithRange(final String label, final List<Integer> data,
                                                           final int range) {
        final List<Integer> nullSafetyNumberRows = getNullSafetyList(data);

        final Integer max = getMaxIntegerNumber(nullSafetyNumberRows) + range;
        final Integer min = getMinIntegerNumber(nullSafetyNumberRows, max) + range;

        return getGeneralizedIntegers(label, nullSafetyNumberRows, max, min);
    }

    //Generalize Double
    public static List<String> generalizeDoubles(final String label, final List<Double> data) {
        final List<Double> nullSafetyNumberRows = getNullSafetyList(data);

        final Integer range = getRandomRange();

        final Double max = getMaxDoubleNumber(nullSafetyNumberRows) + range;
        final Double min = getMinDoubleNumber(nullSafetyNumberRows, max) + range;

        return getGeneralizedDoubles(label, nullSafetyNumberRows, max, min);
    }

    public static List<String> generalizeDoublesWithRange(final String label, final List<Double> data,
                                                          final int range) {
        final List<Double> nullSafetyNumberRows = getNullSafetyList(data);

        final Double max = getMaxDoubleNumber(nullSafetyNumberRows) + range;
        final Double min = getMinDoubleNumber(nullSafetyNumberRows, max) + range;

        return getGeneralizedDoubles(label, nullSafetyNumberRows, max, min);
    }

    //Generalize Strings
    public static List<String> generalizeStrings(final List<String> data) {
        validateStringGeneralization(data);

        final int level = getLowestIndexWithoutSimilarity(data);

        final List<String> generalizedData = new ArrayList<>();

        for (final String row : data) {
            generalizedData.add(SuppressorUtil.suppressWithLevel(row, level));
        }

        return generalizedData;
    }

    public static List<String> generalizeStringsWithLevel(final List<String> data, int level) {
        validateStringGeneralization(data);

        if (!checkIfLevelIsValid(data, level)) {
            return null;
        }

        final List<String> generalizedData = new ArrayList<>();

        for (final String row : data) {
            generalizedData.add(SuppressorUtil.suppressWithLevel(row, level));
        }

        return generalizedData;
    }

    //Generalize dates
    public static List<String> generalizeDates(final String label, final List<String> data) {
        final List<String> nullSafetyStringRows = getNullSafetyList(data);
        final List<Date> dateList = getDateList(nullSafetyStringRows);

        final int range = getRandomRange();

        return getDateResults(label, range, dateList);
    }

    public static List<String> generalizeDatesWithRange(final String label, final List<String> data, final int range) {
        final List<String> nullSafetyStringRows = getNullSafetyList(data);
        final List<Date> dateList = getDateList(nullSafetyStringRows);

        return getDateResults(label, range, dateList);
    }
}
