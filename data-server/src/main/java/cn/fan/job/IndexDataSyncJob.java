package cn.fan.job;

import java.util.List;
import cn.fan.service.IndexNameService;
import cn.hutool.core.date.DateUtil;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import cn.fan.pojo.IndexName;
import cn.fan.service.IndexService;

//定时任务
public class IndexDataSyncJob extends QuartzJobBean {

    @Autowired
    private IndexService indexService;

    @Autowired
    private IndexNameService indexNameService;

    //定时器：获取今天的数据并且刷新redis中
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        System.out.println("定时启动：" + DateUtil.now());

        indexNameService.removeNames();
        List<IndexName> indexNames=indexNameService.getAll();
        for(IndexName indexName:indexNames){
            indexService.fresh(indexName.getCode());
        }

        System.out.println("定时结束：" + DateUtil.now());
    }


}
