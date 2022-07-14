package cn.fan.dao;

import cn.fan.pojo.TaskRemind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRemindDao  extends JpaRepository<TaskRemind,Integer> {
    List<TaskRemind> findByIndexcodeAndIsnotice(String indexcode,int isnotice);
    TaskRemind findFirstById(int id);
}
