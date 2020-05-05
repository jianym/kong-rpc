package org.jeecf.kong.rpc.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeecf.common.lang.StringUtils;

/**
 * 资源定位工具类
 * 
 * @author jianyiming
 *
 */
public class ResourceLocationUtils {

    private static String pattern = "[A-Za-z0-9-~\\/]{1,255}";

    private static Pattern p = Pattern.compile("\\/+");

    public static boolean check(String resource) {
        return Pattern.matches(pattern, resource);
    }

    public static String buildPath(String basePath, String path) {
        path = "/" + path;
        if (StringUtils.isNotEmpty(basePath)) {
            path = "/" + basePath + path;
        }
        Matcher m = p.matcher(path);
        return m.replaceAll("\\/");
    }

}
