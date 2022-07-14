package cn.fan.mapper;

import cn.fan.pojo.IndexPerson;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface IndexPersonMapper {
    @Select(" select * from indexperson where code=#{code} order by buytime asc ")
    List<IndexPerson> findByCode(String code);

}
