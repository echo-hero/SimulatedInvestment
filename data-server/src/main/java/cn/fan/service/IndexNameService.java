package cn.fan.service;

import cn.fan.dao.IndexNameDao;
import cn.fan.pojo.IndexName;
import cn.fan.util.SpringContextUtil;
import cn.hutool.core.collection.CollUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@CacheConfig(cacheNames="indexName")
public class IndexNameService {
    private Map<String, List<IndexName>> indexeNameDatas=new HashMap<>();
    @Autowired IndexNameDao indexNameDao;

    //刷新数据
    //@HystrixCommand(fallbackMethod = "third_part_not_connected")
    public List<IndexName> fresh(String fundcode) {
        List<IndexName> indexNames =indexNameDao.findByCode(fundcode);

        indexeNameDatas.put(fundcode, indexNames);

        IndexNameService indexNameService = SpringContextUtil.getBean(IndexNameService.class);
        indexNameService.remove(fundcode);
        return indexNameService.store(fundcode);
    }

    //清空缓存（redis）
    @CacheEvict(key="'indexData-name-'+ #p0")
    public void remove(String fundcode){

    }

    //保存数据
    @Cacheable(key="'indexData-name-'+ #p0")
    public List<IndexName> store(String code){
        return indexeNameDatas.get(code);
    }

    //获取数据
    @Cacheable(key="'indexData-name-'+ #p0")
    public List<IndexName> get(String fundcode){
        if(null==fundcode) return null;
        if(CollUtil.toList().size()==0)//无数据时，使用刷新数据方式进行获取
            return fresh(fundcode);
        else
            return CollUtil.toList();
    }

    @CacheEvict(key="'indexNameDatas'")
    public void removeNames(){
    }

    //获取全部数据
    @Cacheable(key="'indexNameDatas'")
    public List<IndexName> getAll(){
        List<IndexName> indexNameList = indexNameDao.findAll();
        IndexNameService indexNameService = SpringContextUtil.getBean(IndexNameService.class);
        for(IndexName indexName:indexNameList){
            indexNameService.get(indexName.getCode());
        }
//        System.out.println("getALL方法！");
        return indexNameList;
    }

//    //刷新全部数据
//    @Cacheable(key="'indexNameDatas'")
//    public List<IndexName> freshs(){
//        removeNames();
//        List<IndexName> indexNameList=getAll();
//        indexeNameDatas.clear();
//        for(IndexName indexName:indexNameList){
//            fresh(indexName.getCode());
//        }
//        return getAll();
//    }

//    public List<IndexName> getAllBySQL(){
//        return indexNameDao.findAll();
//    }

    public List<IndexName> findByCode(String code){
        return indexNameDao.findByCode(code);
    }

    public IndexName add(String code,String name){
        IndexName indexName=new IndexName();
        indexName.setCode(code);
        indexName.setName(name);
        indexNameDao.save(indexName);
        return indexName;
    }

}
