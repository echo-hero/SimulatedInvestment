package cn.fan.dao;

import cn.fan.pojo.TaskGrid;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskGridDao extends JpaRepository<TaskGrid,Integer> {
    TaskGrid findFirstByIndexcode(String indexcode);
    TaskGrid findFirstByRisetaskid(int risetaskid);
    TaskGrid findFirstByFalltaskid(int falltaskid);
}
