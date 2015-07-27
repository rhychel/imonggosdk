package net.nueca.imonggosdk.tools;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by rhymart on 7/27/15.
 * imonggosdk (c)2015
 */
public class StringUtilsEx extends StringUtils {

    public static String ucwords(String str) {
        String[] words = str.split(" ");
        StringBuilder finalWords = new StringBuilder();
        int space = 0;
        for(String word : words) {
            if(space > 0)
                finalWords.append(" ");
            finalWords.append(StringUtils.capitalize(word));
            space++;
        }
        return finalWords.toString();
    }

}
