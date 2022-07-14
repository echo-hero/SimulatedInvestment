package cn.fan.mapper;

import cn.fan.pojo.Index;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface IndexMapper {
    @Select(" select * from indexmain where code=#{code} and jzrq=#{jzrq} order by id desc ")
    List<Index> findByCodeAndJzrq(String code, String jzrq);


    @Select(" select * from indexmain where code=#{code} and jzrq<=#{jzrq} order by jzrq desc limit 0,1 ")
    Index firstByCodeAndJzrq(String code, String jzrq);
}
