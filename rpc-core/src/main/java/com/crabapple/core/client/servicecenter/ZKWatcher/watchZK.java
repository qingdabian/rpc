package com.crabapple.core.client.servicecenter.ZKWatcher;


import com.crabapple.core.client.cache.ServiceCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;

@Slf4j
public class watchZK {
    private CuratorFramework client;
    private ServiceCache serviceCache;

    public watchZK(CuratorFramework client, ServiceCache serviceCache) {
        this.client = client;
        this.serviceCache = serviceCache;
    }

    public void watch(String path) {
        CuratorCache curatorCache = CuratorCache.build(client, "/" +path);
        curatorCache.listenable().addListener(new CuratorCacheListener() {
            @Override
            public void event(Type type, ChildData childData, ChildData childData1) {
                switch (type.name()) {
                    case "NODE_CREATED":
                        String[] pathlist = parsepath(childData1);
                        if(pathlist.length<=2) break;
                        else{
                            String servicename=pathlist[1];
                            String adress=pathlist[2];
                            serviceCache.addServiceToCache(servicename,adress);
                        }
                        break;
                    case "NODE_CHANGE":
                        if(childData!=null){
                            log.info("修改前数据:{}",childData.getData());
                        }else{
                            log.info("节点第一次被创建");
                        }
                        String[] oldpathlist = parsepath(childData);
                        String[] newpathlist = parsepath(childData1);
                        serviceCache.replaceServiceToCache(newpathlist[1],oldpathlist[2],newpathlist[2]);
                        log.info("修改后数据:{}",childData1.getData());
                        break;
                    case "NODE_DELETED":
                        String[] deletepathlist = parsepath(childData);
                        if(deletepathlist.length<=2) break;
                        else{
                            String servicename=deletepathlist[1];
                            String adress=deletepathlist[2];
                            serviceCache.removeServiceFromCache(servicename);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
     curatorCache.start();
    }

    private String[] parsepath(ChildData childData) {
        String path = childData.getPath();
        return path.split("/");
    }
}

