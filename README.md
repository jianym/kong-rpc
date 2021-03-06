# kong-rpc
kong-rpc采用单一连接模式，使用kyro序列化，通过读写超时关闭连接。kong-rpc支持负载均衡，断路器，多服务端，重试，多版本控制，同步异步调用等
## 快速开始
### 服务端接入
1.开启服务端 @EnableKrpcRegister

``` 
@SpringBootApplication
@EnableKrpcRegister
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
``` 

2.暴露服务

``` 
@KrpcServer
@RequestMapping(value="/test")
public class TestController {
    
    @RequestMapping("/hello")
    public String hello(String hello) {
        return hello+" world";
    }
}
```

3.服务端参数配置

``` 
krpc:
   server:
      port: 20415
      zookeeper:
         address: localhost:2181
         namespace: test
         #auth: admin:123 
```
### 客户端接入
1.开启客户端 @EnableKrpcDiscover

``` 
@SpringBootApplication
@EnableKrpcDiscover
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
```
2.客户端调用

``` 
@KrpcClient(“/test”)
public interface TestSservice {
    
    @Krpc("/hello")
    public String hello(String hello);

}
```
```
@RestController
@RequestMapping("/test")
public class TestController {

    @KrpcAutowired
    private TestSservice testService;

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String hello() {
        return testService.hello("hello")
    }
    
}
 ```
 3.客户端参数配置
 
``` 
krpc:
   client:
      zookeeper:
         address: localhost:2181
         namespace: test
         #auth: admin:123 
```
## 负载均衡
可以配置轮询（loop）,随机（random）,参数哈希（hash），默认轮询

``` 
krpc:
   client:
      route: hash
```
## 重试，响应超时与版本
服务端

``` 
@KrpcServer
@RequestMapping(value="/test")
public class TestController {
    
    @RequestMapping(value="/hello")
    public String hello(String hello) {
        return hello+" world";
    }
    
    @RequestMapping(value="/hello",version=1)
    public String hello1(String hello) {
        return hello+" world1";
    }
}
```
客户端，默认重试2次，超时3秒

``` 
@KrpcClient(“/test”)
public interface TestSservice {
    
    @Krpc("/hello")
    public String hello(String hello);
    
    @Krpc("/hello",version=1,retry=2,timeout=3000)
    public String hello1(String hello);

}
```
## 断路器
拦截器默认不接入，设置enable为true接入

``` 
krpc:
   client:
      breaker:
         enable: true
         max: 20 #失败阀值
         rate： 50 #失败比率
         window： 300000 #检测窗口
         sleep:  1800000 #断路打开后，多久进入半打开状态
         interval: 3000 #健康检测时间间隔
         bucket： #桶，检测窗口划分为多少桶
```

## 多服务端

基于别名配置

``` 
krpc:
   client:
      alias:
         - name: test
           zookeeper:
              address: localhost:2181
              namespace: jym
```

客户端调用

``` 
@KrpcClient(“/test”)
public interface TestSservice {
    
    @Krpc(value="/hello",alias="test")
    public String hello(String hello);
    
    @Krpc("/hello",version=1,retry=2,timeout=3000)
    public String hello1(String hello);

}
```
## 异步调用

``` 
@KrpcClient(“/test”)
public interface TestSservice {
    
    @Krpc(value="/hello",alias="test")
    public String hello(String hello);
    
    @Krpc("/hello",version=1,retry=2,timeout=3000)
    public Future<String> hello1(String hello);

}
```
## 功能增强
服务端

``` 
@KrpcServerAdvice
public class ControllerAdrise {
    
    @BeforeHandler
    public void handler(BeforeJoinPoint joinPoint) {
        System.out.println("前置拦截--------------");
    }

    @AfterHandler
    public void handler(AfterJoinPoint joinPoint) {
        System.out.println("后置拦截--------------";
    }
    
    @AroundHandler
    public Object handler(AroundJoinPoint joinPoint) throws Throwable {
       System.out.println("方法前置----------------");
       Object result= joinPoint.processon();
       System.out.println("方法后置----------------");
       return result;
    }
    
    @ExceptionHandler
    public Object handler(ExceptionJoinPoint joinPoint) {
        return "66666";   
    }
}
```
客户端

``` 
@KrpcClientAdvice
public class KrpcServiceAdrise {
    
    @BeforeHandler
    public void handler(BeforeJoinPoint joinPoint) {
        System.out.println("前置拦截--------------");
    }

    @AfterHandler
    public void handler(AfterJoinPoint joinPoint) {
        System.out.println("后置拦截--------------";
    }
   
    ....
}
```
## 客户端非注解调用
``` 
KrpcClient.builder().setRetry(3).build().execution("/test/hello",  json);
```
## 参数调优
### 线程池

服务端

``` 
krpc:
   server:
      thread: 
         core: 100 #线程数
         queue: 20000  #队列数
```
客户端

``` 
krpc:
   client:
      thread: 
         core: 100
         queue: 20000
```
### 通信

服务端

``` 
krpc:
   server:
      socket: 
         low: 32768 # bytebuf 低水位
         height: 65536  #bytebuf 高水位
         timeout: 3000 #读超时
         back：50 #tcp接收队列大小
```

客户端

``` 
krpc:
   server:
      socket: 
         low: 32768 # bytebuf 低水位
         height: 65536  #bytebuf 高水位
         timeout: 3000 #写超时
         connection：true  #初始化建立连接
```

