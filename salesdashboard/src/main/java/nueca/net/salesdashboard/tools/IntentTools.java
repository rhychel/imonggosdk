package nueca.net.salesdashboard.tools;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import nueca.net.salesdashboard.R;
import nueca.net.salesdashboard.enums.ShareType;

/**
 * Jn Cld
 * Imonggo Sales Dashboard(c) 2015
 */
public class IntentTools {

    public static Intent getIntentByType(Context context, ShareType shareType) {
        PackageInfo info = null;
        String url = context.getString(R.string.url_nueca);
        String url_id = context.getString(R.string.url_nueca);

        switch (shareType) {
            case FACEBOOK:
                try {
                    info = context.getPackageManager().getPackageInfo("com.facebook.katana", 0);
                } catch (PackageManager.NameNotFoundException e) {
                    return new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.url_facebook_id)));
                }
                url = context.getString(R.string.url_facebook);
                url_id = context.getString(R.string.url_facebook_id);
                break;
            case TWITTER:
                try {
                    info = context.getPackageManager().getPackageInfo("com.twitter.android", 0);
                } catch (PackageManager.NameNotFoundException e) {
                    return new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.url_twitter)));
                }

                url = context.getString(R.string.url_twitter);
                url_id = context.getString(R.string.url_twitter_id);
                break;
            case INSTAGRAM:
                try {
                    info = context.getPackageManager().getPackageInfo("com.instagram.android", 0);
                } catch (PackageManager.NameNotFoundException e) {
                    return new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.url_instagram)));
                }

                url = context.getString(R.string.url_instagram);
                url_id = context.getString(R.string.url_instagram_id);
                break;
            case REPORT:

                Intent intent=new Intent(Intent.ACTION_SEND);
                String[] recipients={"jan@nueca.net"};
                intent.putExtra(Intent.EXTRA_EMAIL, recipients);
                intent.putExtra(Intent.EXTRA_SUBJECT,context.getString(R.string.subject));
                intent.putExtra(Intent.EXTRA_CC, context.getString(R.string.cc));
                intent.setType("message/rfc822");
                return intent;
        }

        if (info.applicationInfo.enabled) {
            return new Intent(Intent.ACTION_VIEW, Uri.parse(url_id));
        } else {
            return new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        }
    }

}
