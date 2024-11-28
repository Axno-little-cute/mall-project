package com.hmall.gateway.routers;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * @author axno
 * @date 2024/10/28 17:14
 * @DESCRIPTION
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicRouteLoader {

    private final RouteDefinitionWriter writer;
    private final NacosConfigManager nacosConfigManager;
    private final String dataId = "gateway-routes.json";
    private final String group = "DEFAULT_GROUP";
    private final Set<String> routeIds = new HashSet<>();

    //项目一启动就执行
    @PostConstruct //作用：在这个Bean初始化之后执行
    public void initRouteConfigListener() throws NacosException {
        //1.项目启动时，先拉取一次配置，并且添加监听器
        String configInfo = nacosConfigManager.getConfigService()
                .getConfigAndSignListener(dataId, group, 5000, new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return null;
                    }
                    //监听到配置变更
                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        //更新路由表
                        updateConfigInfo(configInfo);
                    }
                });
        //第一次读取到配置，也需要更新到路由表
        updateConfigInfo(configInfo);
    }

    /**
     * 将配置信息更新到路由表
     * @param configInfo 配置信息
     */
    public void updateConfigInfo(String configInfo){
        log.debug("监听到路由配置信息：{}",configInfo);
        //1解析配置信息，装为RouteDefinition
        List<RouteDefinition> routeDefinitions = JSONUtil.toList(configInfo, RouteDefinition.class);

        //1.5 不管你之前的配置 直接把旧的路由表删除了，再添加新的
        //要传入路由id才能删除，但怎么知道呢？ 路由id就是上一次更新的路由表的id
        for (String routeId : routeIds) {
            writer.delete(Mono.just(routeId)).subscribe();
        }

        //清空路由id表 便于进行下一次更新记录路由id
        routeIds.clear();

        //2更新路由表
        for (RouteDefinition routeDefinition : routeDefinitions) {
            //2.1
            //save方法需要传入一个Mono，Mono是springboot提供的一个容器 Mono.just 就是它
            writer.save(Mono.just(routeDefinition)).subscribe(); //一定要subscribe()订阅
            //2.2 记录路由id，便于下一次更新时删除。
            routeIds.add(routeDefinition.getId());
        }
    }
}
