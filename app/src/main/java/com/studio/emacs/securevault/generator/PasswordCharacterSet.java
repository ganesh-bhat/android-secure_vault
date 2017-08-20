package com.studio.emacs.securevault.generator;

/**
 * Created by ganbhat on 11/11/2016.
 */

public interface PasswordCharacterSet {
    char[] getCharacters();
    int getMinCharacters();
}
