package im.vector.app.kelare.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.WebSettings;

import timber.log.Timber;

/**
 * author : Jason
 * date   : 4/19/22 10:49 AM
 * desc   :
 */
public class APPUtil {

    /**
     * 返回正确的UserAgent
     * @return
     */
    @SuppressLint("ObsoleteSdkInt")
    public static String getUserAgent(Context context){
        String userAgent = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                userAgent = WebSettings.getDefaultUserAgent(context);
            } catch (Exception e) {
                userAgent = System.getProperty("http.agent");
            }
        } else {
            userAgent = System.getProperty("http.agent");
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0, length = userAgent.length(); i < length; i++) {
            char c = userAgent.charAt(i);
            if (c <= '\u001f' || c >= '\u007f') {
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                sb.append(c);
            }
        }

        Timber.tag("User-Agent:").e(sb.toString());
        return sb.toString();
    }

    
}
