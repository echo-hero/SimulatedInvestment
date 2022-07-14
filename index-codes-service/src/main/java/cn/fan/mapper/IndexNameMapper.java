package cn.fan.mapper;


import cn.fan.pojo.IndexName;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface IndexNameMapper {
    @Select(" select * from indexname where code in (select indexcode from taskremind) ")
    List<IndexName> findTaskRemind();
}
