package cn.fan.job;

import cn.fan.mapper.IndexMapper;
import cn.fan.pojo.Index;
import cn.fan.service.EmailRemindService;
import cn.fan.util.SendmailUtil;
import cn.fan.util.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RemindJob{

    @Autowired IndexMapper indexMapper;

    @Autowired EmailRemindService emailRemindService;

    @Scheduled(cron = "0 0 0/1 * * ? ")
    protected void executeInternal() {
        System.out.println("邮件提醒发送检测自动任务！");

        List<Index> indexList=indexMapper.findNewIndex();
//        System.out.println("size:"+indexList.size());
        for(Index index:indexList){
            emailRemindService.remind(index);
        }
    }
}