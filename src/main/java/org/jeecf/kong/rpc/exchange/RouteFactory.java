package org.jeecf.kong.rpc.exchange;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jeecf.kong.rpc.common.SpringContextUtils;
import org.jeecf.kong.rpc.common.exception.NotFoundAliasException;
import org.jeecf.kong.rpc.common.exception.NotMatchRouteException;
import org.jeecf.kong.rpc.discover.properties.KrpcClientProperties;
import org.jeecf.kong.rpc.discover.properties.KrpcProperties;

/**
 * 获取路由 工厂
 * 
 * @author jianyiming
 *
 */
public class RouteFactory {

    private static KrpcClientProperties properties = SpringContextUtils.getBean(KrpcClientProperties.class);

    public static Route getRoute(String alias, String data) {
        String route = "";
        if (StringUtils.isNotEmpty(alias) && !alias.equals(properties.getName())) {
            List<KrpcProperties> proList = properties.getAlias();
            Iterator<KrpcProperties> its = proList.iterator();
            if (its.hasNext()) {
                KrpcProperties pro = its.next();
                if (pro.getName().equals(alias)) {
                    route = pro.getRoute();
                }
            }
        } else {
            route = properties.getRoute();
        }
        if (StringUtils.isEmpty(route))
            throw new NotFoundAliasException("alias " + alias + "not found...");
        if (route.equals("random")) {
            return RandomRoute.getInstance();
        } else if (route.equals("loop")) {
            return LoopRoute.getInstance();
        } else if (route.equals("hash") && data.equals("{}")) {
            return RandomRoute.getInstance();
        } else if (route.equals("hash")) {
            return ConsistencyHashRoute.getInstance();
        }
        throw new NotMatchRouteException("route " + route + "not match...");
    }

}
