package cn.fan.service;

import cn.fan.mapper.IndexMapper;
import cn.fan.pojo.Index;
import cn.hutool.core.collection.CollUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames="indexes")
public class IndexService {
    @Autowired
    IndexMapper indexMapper;

    @Cacheable(key="'indexData-code-'+ #p0")
    public List<Index> get(String fundcode){
        Index index = new Index();
        index.setCode("000000");
        return CollUtil.toList(index);
    }

    public List<Index> findByCodeAndJzrq(String code, String jzrq){
        return indexMapper.findByCodeAndJzrq(code,jzrq);
    }

    public Index firstByCodeAndJzrq(String code, String jzrq){ return indexMapper.firstByCodeAndJzrq(code, jzrq); }
}
