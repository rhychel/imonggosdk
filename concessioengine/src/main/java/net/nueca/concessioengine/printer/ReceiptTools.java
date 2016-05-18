package net.nueca.concessioengine.printer;

/**
 * Created by rhymartmanchus on 17/05/2016.
 */
public class ReceiptTools {

    public static String spacer(String text1, String text2, int maxChar) {
        String finalText = text1+text2;
        int combinedLength = text1.length()+text2.length();
        if(combinedLength < maxChar) {
            int spaces = maxChar-combinedLength;
            String space = "";
            for(int i = 0;i < spaces;i++)
                space += " ";

            finalText = text1+space+text2;
        }
        return finalText;
    }

    public static String addSpace(int spaces) {
        String addSpaces = "";
        for(int i = 0;i < spaces;i++)
            addSpaces += " ";
        return addSpaces;
    }

    public static String tabber(String textTabber, String text2, int maxChar) {
        String finalText = textTabber+text2;
        int tabSpace = textTabber.length();

        if(textTabber.length()+text2.length() <= maxChar)
            return finalText;

        finalText = "";
        String[] splitted = (textTabber+text2).split(" ");
        String append = "";
        boolean next = false;
        for(String val : splitted) {
            if(next)
                append += " ";
            if((append+val+" ").length() >= 33) {
                finalText += append+"\n";
                append = "";
                append += addSpace(tabSpace);
            }
            append += val;

            next = true;
        }
        finalText += append;

        return finalText;
    }

}
