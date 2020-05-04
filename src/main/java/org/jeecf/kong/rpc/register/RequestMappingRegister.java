package org.jeecf.kong.rpc.register;

import java.lang.reflect.Method;

import org.jeecf.kong.rpc.common.ResourceLocationUtils;
import org.jeecf.kong.rpc.common.exception.ResourceLocationFormatException;
import org.jeecf.kong.rpc.register.ProviderContainer.RequestServerNode;
import org.jeecf.kong.rpc.register.annotation.RequestMapping;
import org.springframework.stereotype.Component;

/**
 * 资源定位 注册
 * 
 * @author jianyiming
 *
 */
@Component
public class RequestMappingRegister {

    private ProviderContainer providerContainer = ProviderContainer.getInstance();

    public void register(Class<?> clazz, Object bean) {
        String basePath = "";
        RequestMapping cRequest = clazz.getAnnotation(RequestMapping.class);
        int version = 0;
        if (cRequest != null) {
            basePath = cRequest.value();
            version = cRequest.version();
        }
        Method[] methods = clazz.getMethods();
        for (int k = 0; k < methods.length; k++) {
            RequestMapping mRequest = methods[k].getAnnotation(RequestMapping.class);
            if (mRequest != null) {
                String path = ResourceLocationUtils.buildPath(basePath, mRequest.value());
                if (!ResourceLocationUtils.check(path)) {
                    throw new ResourceLocationFormatException("path format is error " + path);
                }
                if (mRequest.version() != 0) {
                    version = mRequest.version();
                }
                RequestServerNode node = providerContainer.new RequestServerNode();
                node.setMethod(methods[k]);
                node.setVersion(version);
                node.setInstance(bean);
                node.setPath(path);
                providerContainer.add(version+"_"+path, node);
            }
        }
    }

}
