package com.studio.emacs.securevault.safe;

/**
 * Created by ganbhat on 11/12/2016.
 */

import java.util.ArrayList;

/**
 *
 * @author Steven Osborn - http://steven.bitsetters.com
 */
public class PassEntry extends Object {
    public String password;
    public long id;
    public long category;
    public String categoryName;
    public String description;
    public String username;
    public String website;
    public String uniqueName;
    // public ArrayList<String> packageAccess;
    public String note;
    public String plainPassword;
    public String plainDescription;
    public String plainUsername;
    public String plainWebsite;
    public String plainNote;

    public static boolean checkPackageAccess (ArrayList<String> packageAccess, String packageName) {
        return (packageAccess.contains(packageName));
    }
}
