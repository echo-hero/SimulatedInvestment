package cn.fan.service;

import cn.fan.pojo.IndexName;
import cn.hutool.core.collection.CollUtil;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames="indexName")
public class IndexNameService {

    @Cacheable(key="'indexData-name-'+ #p0")
    public List<IndexName> get(String fundcode){
        IndexName indexName=new IndexName();
        indexName.setCode("000000");
        indexName.setName("未成功获取到数据！");
        return CollUtil.toList(indexName);
    }

    @Cacheable(key="'indexNameDatas'")
    public List<IndexName> getAll(){
        IndexName indexName=new IndexName();
        indexName.setCode("000000");
        indexName.setName("未成功获取到数据！");
        return CollUtil.toList(indexName);
    }

//    @Cacheable(key="'indexNameDatas'")
//    public Page4Navigator<IndexName> getPage(int start, int size, int navigatePages){
//        Sort sort=new Sort(Sort.Direction.DESC, "id");
//        Pageable pageable = new PageRequest(start, size,sort);
//        Page pageFromJPA
//
//    }
}
