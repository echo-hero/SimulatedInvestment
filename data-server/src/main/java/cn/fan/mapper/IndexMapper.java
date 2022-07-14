package cn.fan.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;

import cn.fan.pojo.Index;

@Mapper
public interface IndexMapper {

    @Select(" select * from indexmain where code=#{code} and jzrq=#{jzrq} ")
    List<Index> findByCodeAndJzrq(String code,String jzrq);

    @Select(" select * from indexmain where code=#{code} order by jzrq asc ")
    List<Index> findByCode(String code);

    @Insert(" insert into indexmain values (#{id},#{code},#{jzrq},#{dwjz},#{gztime}) ")
    int insertIndex(String id,String code,String jzrq,String dwjz,String gztime);

    @Select(" select * from indexmain m,(select code,max(jzrq) rq from indexmain group by code) maxm where m.code=maxm.code and m.jzrq=maxm.rq ")
    List<Index> findNewIndex();
}
