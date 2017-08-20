package com.studio.emacs.securevault.generator;

/**
 * Created by ganbhat on 11/11/2016.
 */

import com.studio.emacs.securevault.generator.PasswordCharacterSet;
import com.studio.emacs.securevault.generator.PasswordGenerator;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class PasswordCharSets {
    private static final char[] ALPHA_UPPER_CHARACTERS = { 'A', 'B', 'C', 'D',
            'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
            'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
    private static final char[] ALPHA_LOWER_CHARACTERS = { 'a', 'b', 'c', 'd',
            'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
            'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
    private static final char[] NUMERIC_CHARACTERS = { '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9' };
    private static final char[] SPECIAL_CHARACTERS = { '~', '`', '!', '@', '#',
            '$', '%', '^', '&', '*', '(', ')', '-', '_', '=', '+', '[', '{',
            ']', '}', '\\', '|', ';', ':', '\'', '"', ',', '<', '.', '>', '/',
            '?' };

    public static PasswordGenerator getPasswordGenerator(int min, int max, int fixedLength, SummerCharSet[] charSets) {
        Set<PasswordCharacterSet> values = null;
        if(charSets.length>0) {
            if(charSets.length == 1) {
                values = new HashSet<PasswordCharacterSet>(EnumSet.of(charSets[0]));
            } else {
                values = new HashSet<PasswordCharacterSet>(EnumSet.of(charSets[0], Arrays.copyOfRange(charSets,1,charSets.length)));
            }
        }
        return new PasswordGenerator(values, min, max,fixedLength);
    }


    public enum SummerCharSet implements  PasswordCharacterSet{
        ALPHA_UPPER(ALPHA_UPPER_CHARACTERS, 1),
        ALPHA_LOWER(ALPHA_LOWER_CHARACTERS, 1),
        NUMERIC(NUMERIC_CHARACTERS, 1),
        SPECIAL(SPECIAL_CHARACTERS, 1);

        private final char[] chars;
        private final int minUsage;

        private SummerCharSet(char[] chars, int minUsage) {
            this.chars = chars;
            this.minUsage = minUsage;
        }

        @Override
        public char[] getCharacters() {
            return chars;
        }

        @Override
        public int getMinCharacters() {
            return minUsage;
        }
    }


}