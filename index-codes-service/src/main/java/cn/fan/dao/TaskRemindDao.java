package cn.fan.dao;

import cn.fan.pojo.TaskRemind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRemindDao extends JpaRepository<TaskRemind,Integer> {
    public List<TaskRemind> findByIndexcodeAndIsnotice(String indexcode, int isnotice);
    public TaskRemind getFirstById(int id);
}
