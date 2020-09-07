package tech.introduct.mailbox.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

@UtilityClass
public class EstonianIdUtils {

    public static String addEEIfValid(String number) {
        if (number.length() == 11 && EstonianIdUtils.isValid(number)) {
            return "EE" + number;
        }
        return number;
    }

    public static boolean isValid(String number) {
        if (number == null) {
            return false;
        }
        if (number.length() == 13) {
            number = number.substring(2);
        }
        return number.length() == 11
                && StringUtils.isNumeric(number)
                && calculateChecksum(number) == NumberUtils.toInt(number.substring(10));
    }

    private static int calculateChecksum(String number) {
        int remainder = calculateWeightRemainder(number, new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 1});
        if (remainder < 10) {
            return remainder;
        } else {
            remainder = calculateWeightRemainder(number, new int[]{3, 4, 5, 6, 7, 8, 9, 1, 2, 3});
            return remainder < 10 ? remainder : 0;
        }
    }

    private static int calculateWeightRemainder(String number, int[] weights) {
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += NumberUtils.toInt(number.substring(i, i + 1)) * weights[i];
        }
        return sum % 11;
    }
}
