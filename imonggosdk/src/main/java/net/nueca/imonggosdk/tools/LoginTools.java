package net.nueca.imonggosdk.tools;

import android.content.Context;
import android.text.TextUtils;

import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.operations.ImonggoTools;

/**
 * Created by Jn on 6/9/2015.
 * imonggosdk (c)2015
 */
public class LoginTools {

    /**
     * Check if the email is valid
     *
     * @param email
     * @return true if not null and matches the pattern, false otherwise.
     */
    public static boolean isValidEmail(CharSequence email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Checks if the password is valid
     *
     * @param password
     * @return true if length of password is greater or equal than 5, false otherwise.
     */
    public static boolean isValidPassword(CharSequence password) {
        return password.length() >= 4;
    }

    /**
     *  Returns
     *
     * @param context
     * @param server
     * @param accountId
     * @return
     */
    public static String getAPIUrl(Context context, Server server, String accountId) {
        switch (server) {
            case IMONGGO:
                return ImonggoTools.buildAPIUrlImonggo(context, accountId);
            case PLDTRETAILCLOUD:
                return ImonggoTools.buildAPIUrlPLDTRetailCloud(context, accountId);
            case IRETAILCLOUD_COM:
                return ImonggoTools.buildAPIUrlIRetailCloudCom(context, accountId);
            case IRETAILCLOUD_NET:
                return ImonggoTools.buildAPIUrlIRetailCloudNet(context, accountId);
            case IMONGGO_NET:
                return ImonggoTools.buildAPIUrlImonggoNet(context, accountId);
            case PETRONDIS_COM:
                return ImonggoTools.buildAPIUrlPetrondisCOM(context, accountId);
            case PETRONDIS_NET:
                return ImonggoTools.buildAPIUrlPetrondisNET(context, accountId);
            case REBISCO_DEV:
                return ImonggoTools.buildAPIUrlRebiscoDev(context, accountId);
            case REBISCO_LIVE:
                return ImonggoTools.buildAPIUrlRebiscoLive(context, accountId);
            default:
                return "";
        }
    }
}