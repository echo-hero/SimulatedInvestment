package cn.fan.dao;

import cn.fan.pojo.IndexName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IndexNameDao extends JpaRepository<IndexName,Integer> {
    public List<IndexName> findByCode(String code);
}
