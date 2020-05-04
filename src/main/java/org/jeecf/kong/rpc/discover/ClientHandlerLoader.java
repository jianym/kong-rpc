package org.jeecf.kong.rpc.discover;

import java.lang.reflect.Method;

import org.jeecf.kong.rpc.common.annotation.AfterHandler;
import org.jeecf.kong.rpc.common.annotation.AroundHandler;
import org.jeecf.kong.rpc.common.annotation.BeforeHandler;
import org.jeecf.kong.rpc.common.annotation.ExceptionHandler;
import org.jeecf.kong.rpc.discover.AfterHandlerContext.AfterNode;
import org.jeecf.kong.rpc.discover.AroundHandlerContext.AroundNode;
import org.jeecf.kong.rpc.discover.BeforeHandlerContext.BeforeNode;
import org.jeecf.kong.rpc.discover.ExceptionHandlerContext.ExceptionNode;
import org.springframework.stereotype.Component;

/**
 * 客户端增强注册
 * 
 * @author jianyiming
 *
 */
@Component
public class ClientHandlerLoader {

    public void load(Class<?> clazz, Object bean) {
        Method[] mArray = clazz.getMethods();
        if (mArray == null || mArray.length == 0) {
            return;
        }
        for (Method m : mArray) {
            BeforeHandler beforeHandler = m.getAnnotation(BeforeHandler.class);
            AfterHandler afterHandler = m.getAnnotation(AfterHandler.class);
            ExceptionHandler exceptionHandler = m.getAnnotation(ExceptionHandler.class);
            AroundHandler aroundHandler = m.getAnnotation(AroundHandler.class);
            if (beforeHandler != null) {
                BeforeHandlerContext conntext = BeforeHandlerContext.getInstance();
                BeforeNode node = conntext.new BeforeNode();
                node.setBasePath(beforeHandler.basePath());
                node.setM(m);
                node.setInstance(bean);
                conntext.addNode(node);
            } else if (afterHandler != null) {
                AfterHandlerContext conntext = AfterHandlerContext.getInstance();
                AfterNode node = conntext.new AfterNode();
                node.setBasePath(afterHandler.basePath());
                node.setM(m);
                node.setInstance(bean);
                conntext.addNode(node);
            } else if (exceptionHandler != null) {
                ExceptionHandlerContext conntext = ExceptionHandlerContext.getInstance();
                ExceptionNode node = conntext.new ExceptionNode();
                node.setBasePath(exceptionHandler.basePath());
                node.setM(m);
                node.setInstance(bean);
                node.setEx(exceptionHandler.ex());
                conntext.addNode(node);
            } else if (aroundHandler != null) {
                AroundHandlerContext conntext = AroundHandlerContext.getInstance();
                AroundNode node = conntext.new AroundNode();
                node.setBasePath(aroundHandler.basePath());
                node.setM(m);
                node.setInstance(bean);
                conntext.addNode(node);
            }
        }
    }

}
