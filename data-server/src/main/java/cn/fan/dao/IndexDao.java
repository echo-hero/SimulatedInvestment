package cn.fan.dao;

import cn.fan.pojo.Index;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IndexDao extends JpaRepository<Index,Integer>{
    List<Index> findByCode(String code);
}
