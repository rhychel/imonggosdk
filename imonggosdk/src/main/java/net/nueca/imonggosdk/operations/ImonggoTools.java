package net.nueca.imonggosdk.operations;

import android.content.Context;
import android.util.Base64;

import net.nueca.imonggosdk.R;
import net.nueca.imonggosdk.enums.Parameter;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.operations.urls.CustomURLTools;
import net.nueca.imonggosdk.tools.Configurations;

/**
 * 
 * ImonggoTools used to build urls, api authentications, date and time formats.
 * 
 * @author rhymart
 */
public class ImonggoTools {

    // String format
    public static String generateParameter(Parameter...parameters){
        String parameterStr = "?";
        int stringInputCount = 1;
        for(Parameter parameter : parameters) {
            switch (parameter) {
                case PAGE:
                    parameterStr += "page=%"+stringInputCount+"$s&";
                    stringInputCount++;
                    break;
                case COUNT:
                    parameterStr += "q=count&";
                    break;
                case LAST_UPDATED_AT:
                    parameterStr += "q=last_updated_at&";
                    break;
                case AFTER:
                    parameterStr += "after=%"+stringInputCount+"$s&";
                    stringInputCount++;
                    break;
                case ACTIVE_ONLY:
                    parameterStr += "active_only=1&";
                    break;
                case USER_ID:
                    parameterStr += "user_id=%"+stringInputCount+"$s&";
                    stringInputCount++;
                    break;
                case CHECKIN_COUNT:
                    parameterStr += "q=checkin_count&";
                    break;
                case BRANCH_ID:
                    parameterStr += "branch_id=%"+stringInputCount+"$s&";
                    stringInputCount++;
                    break;
                case SALES_PUSH:
                    parameterStr += "type=sales_push&";
                    break;
                case FROM:
                    parameterStr += "from=%"+stringInputCount+"$s&";
                    stringInputCount++;
                    break;
                case TO:
                    parameterStr += "to=%"+stringInputCount+"$s&";
                    stringInputCount++;
                    break;
                case TARGET_BRANCH_ID:
                    parameterStr += "target_branch_id=%"+stringInputCount+"$s&";
                    stringInputCount++;
                    break;
                case DOCUMENT_TYPE: // added by Jn
                    parameterStr += "document_type=%"+stringInputCount+"$s&";
                    stringInputCount++;
                    break;
                case INTRANSIT:
                    parameterStr += "intransit_status=%"+stringInputCount+"$s&";
                    stringInputCount++;
                    break;
                case CURRENT_DATE:
                    parameterStr = "";
                    parameterStr += "%"+stringInputCount+"$s.json?";
                    stringInputCount++;
                    break;
                case DETAILS:
                    parameterStr += "q=details&";
                    break;
                case ID:
                    parameterStr = "";
                    parameterStr += "/%"+stringInputCount+"$s.json?";
                    stringInputCount++;
                    break;
            }
        }
        if(!parameterStr.equals("?"))
            parameterStr = parameterStr.substring(0, parameterStr.length()-1);

        return parameterStr;
    }
	
	/**
	 * 
	 * Build Base64 authentication key for the Authorization request property.
	 * 
	 * @param apiToken
	 * @return
	 */
	public static String buildAPIAuthentication(String apiToken) {
		String base = Base64.encodeToString((apiToken+":x").getBytes(), Base64.DEFAULT).trim();
		return base;
	}
	
	/**
	 * 
	 * Build URL for getting Account URL on Imonggo.
	 * 
	 * @param context
	 * @param accountId
	 * @return
	 */
	public static String buildAPIUrlImonggo(Context context, String accountId) {
		return String.format(context.getString(R.string.API_URL_IMONGGO), accountId);
	}

    /**
     *
     * Build URL for getting Account URL on Imonggo.
     *
     * @param context
     * @param accountId
     * @return
     */
    public static String buildAPIUrlImonggoNet(Context context, String accountId) {
        return String.format(context.getString(R.string.API_URL_IMONGGO_NET), accountId);
    }
	
	/**
	 * 
	 * Build URL for getting Account URL on IRetail Cloud.net.
	 * 
	 * @param context
	 * @param accountId
	 * @return
	 */
	public static String buildAPIUrlIRetailCloudNet(Context context, String accountId) {
		return String.format(context.getString(R.string.API_URL_IRETAILCLOUD_NET), accountId);
	}

	/**
	 *
	 * Build URL for getting Account URL on IRetail Cloud.Com.
	 *
	 * @param context
	 * @param accountId
	 * @return
	 */
	public static String buildAPIUrlIRetailCloudCom(Context context, String accountId) {
		return String.format(context.getString(R.string.API_URL_IRETAILCLOUD_COM), accountId);
	}

	/**
	 * 
	 * Build URL for getting Account URL on PLDT Retail Cloud.
	 *
	 * @param context
	 * @param accountId
	 * @return
	 */
	public static String buildAPIUrlPLDTRetailCloud(Context context, String accountId) {
		return String.format(context.getString(R.string.API_URL_PLDTRETAILCLOUD), accountId);
	}

    /**
     *
     * Build URL for getting Account URL on a custom URL.
     *
     * @param context
     * @param accountId
     * @return
     */
    public static String buildAPIUrlCustomURL(Context context, String accountId) {
        return String.format(context.getString(R.string.API_URL_CUSTOM), CustomURLTools.getCustomURL(context), accountId);
    }

    /**
     *
     * Build URL for getting Account URL on a custom URL secured.
     *
     * @param context
     * @param accountId
     * @return
     */
    public static String buildAPIUrlCustomURLSecured(Context context, String accountId) {
        return String.format(context.getString(R.string.API_URL_CUSTOM_SECURED), CustomURLTools.getCustomURL(context), accountId);
    }

	/**
	 * 
	 * Build URL for getting API Token.
	 * 
	 * @param context
	 * @param accountUrl
	 * @param module
	 * @param email
	 * @param password
	 * @return
	 */
	public static String buildAPITokenUrl(Context context, String accountUrl,
										  Table module, String email, String password) {
		return String.format(context.getString(R.string.API_TOKEN_URL), accountUrl,
				Configurations.API_MODULES.get(module), email, password);
	}
	
	/**
	 * 
	 * Build URL for a specific module.
	 * 
	 * @param context
	 * @param apiToken
	 * @param accountUrlNoProtocol
	 * @param module
	 * @param params
	 * @param isSecured
	 * @return
	 */
	public static String buildAPIModuleURL(Context context, String apiToken, String accountUrlNoProtocol,
										   Table module, String params, boolean isSecured) {
		if(isSecured)
			return String.format(context.getString(R.string.API_MODULE_URL_SECURED), apiToken, accountUrlNoProtocol, Configurations.API_MODULES.get(module)+params);
		return String.format(context.getString(R.string.API_MODULE_URL), apiToken, accountUrlNoProtocol, Configurations.API_MODULES.get(module)+params);
	}

    /**
     *
     * Build URL for a specific module with id.
     *
     * @param context
     * @param apiToken
     * @param accountUrlNoProtocol
     * @param module
     * @param id
     * @param params
     * @param isSecured
     * @return
     */
    public static String buildAPIModuleIDURL(Context context, String apiToken, String accountUrlNoProtocol,
											 Table module, String id, String params, boolean isSecured) {
        if(isSecured)
            return String.format(context.getString(R.string.API_MODULE_ID_URL_SECURED), apiToken, accountUrlNoProtocol, Configurations.API_MODULES_ID.get(module), id, params);
        return String.format(context.getString(R.string.API_MODULE_ID_URL), apiToken, accountUrlNoProtocol, Configurations.API_MODULES_ID.get(module), id, params);
    }
	
	/**
	 * 
	 * Build for product image url.
	 * 
	 * @param context
	 * @param apiToken
	 * @param accountUrlNoProtocol
	 * @param productId
	 * @param isLarge
	 * @param isSecured
	 * @return
	 */
	public static String buildProductImageUrl(Context context, String apiToken, String accountUrlNoProtocol, String productId, boolean isLarge, boolean isSecured) {
		if(isSecured) {
			if(isLarge)
				return String.format(context.getString(R.string.PRODUCT_LARGEIMAGE_URL_SECURED), apiToken, accountUrlNoProtocol, productId);
			return String.format(context.getString(R.string.PRODUCT_IMAGE_URL_SECURED), apiToken, accountUrlNoProtocol, productId);
		}
		else {
			if(isLarge)
				return String.format(context.getString(R.string.PRODUCT_LARGEIMAGE_URL), apiToken, accountUrlNoProtocol, productId);
			return String.format(context.getString(R.string.PRODUCT_IMAGE_URL), apiToken, accountUrlNoProtocol, productId);
		}
	}

}
