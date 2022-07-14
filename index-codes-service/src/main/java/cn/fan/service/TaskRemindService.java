package cn.fan.service;


import cn.fan.dao.TaskRemindDao;
import cn.fan.mapper.IndexNameMapper;
import cn.fan.pojo.IndexName;
import cn.fan.pojo.TaskRemind;
import cn.fan.util.SpringContextUtil;
import cn.hutool.core.collection.CollUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@CacheConfig(cacheNames="taskreminds")
public class TaskRemindService {
    @Autowired TaskRemindDao taskRemindDao;

    @Autowired IndexNameMapper indexNameMapper;

    private Map<String,List<TaskRemind>> stringListHashMap=new HashMap<>();

    //清空缓存（redis）
    @CacheEvict(key="'taskremind-'+ #p0")
    public void remove(String indexcode){    }

    //保存数据
    @Cacheable(key="'taskremind-'+ #p0")
    public List<TaskRemind> store(String indexcode){
        return stringListHashMap.get(indexcode);
    }

    //获取数据
    @Cacheable(key="'taskremind-'+ #p0")
    public List<TaskRemind> get(String indexcode){
        if(CollUtil.toList().size()==0)//无数据时，使用刷新数据方式进行获取
            return fresh(indexcode);
        else
            return CollUtil.toList();
    }

    @CacheEvict(key="'taskremind-AllCode'")
    public void removeAllCode(){    }


    @Cacheable(key="'taskremind-AllCode'")
    public List<IndexName> getAllCode(){
        List<IndexName> indexNameList=indexNameMapper.findTaskRemind();
        return indexNameList;
    }

    public List<TaskRemind> fresh(String indexcode){
        TaskRemindService taskRemindService=SpringContextUtil.getBean(TaskRemindService.class);
        taskRemindService.remove(indexcode);

        System.out.println("fresh:"+indexcode);
        List<TaskRemind> taskRemindList=taskRemindDao.findByIndexcodeAndIsnotice(indexcode,0);
        taskRemindList.addAll(taskRemindDao.findByIndexcodeAndIsnotice(indexcode,1));

        System.out.println("taskRemindList.size()："+taskRemindList.size());
        stringListHashMap.put(indexcode,taskRemindList);

        taskRemindService.removeAllCode();
        taskRemindService.getAllCode();
        return store(indexcode);
    }

    public String addTaskRemind(TaskRemind taskRemind) throws Exception{
        if(taskRemind.getIndexcode()=="" || taskRemind.getDwjz()==0 || taskRemind.getAddressee()=="")
            return "存在为空信息!";

        taskRemind.setIsnotice(0);
        taskRemind.setCreatetime(new Date());
        taskRemindDao.save(taskRemind);
        fresh(taskRemind.getIndexcode());
        return "添加成功！";
    }

    public List<TaskRemind> getTaskRemind(int updownflag,int isnotice){
        List<IndexName> indexNameList=getAllCode();

        List<TaskRemind> taskReminds=new ArrayList<>();
        for(IndexName indexName:indexNameList){
            taskReminds.addAll(get(indexName.getCode()));
        }


        List<TaskRemind> taskRemindList=new ArrayList<>();
        for(TaskRemind taskRemind:taskReminds){
            if( (updownflag==-1 && isnotice==-1) ||
                    (updownflag==-1 && isnotice==taskRemind.getIsnotice()) ||
                    (updownflag==taskRemind.getUpdownflag() && isnotice==-1) ||
                    (updownflag==taskRemind.getUpdownflag() && isnotice==taskRemind.getIsnotice()) )
                taskRemindList.add(taskRemind);
        }
        return taskRemindList;
    }

    public TaskRemind getTaskRemindById(int id){
        return taskRemindDao.getFirstById(id);
    }

    public String editTask(TaskRemind taskRemind){
        taskRemindDao.save(taskRemind);
        fresh(taskRemind.getIndexcode());
        return "修改成功！";
    }

}
