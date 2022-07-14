package cn.fan.dao;

import cn.fan.pojo.IndexPerson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IndexPersonDao extends JpaRepository<IndexPerson,Integer> {
    public List<IndexPerson> findByCode(String code);
    public List<IndexPerson> findByCodeAndOperatetime(String code,String operatetime);
}
