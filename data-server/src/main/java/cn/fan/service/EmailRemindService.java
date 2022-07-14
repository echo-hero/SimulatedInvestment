package cn.fan.service;

import cn.fan.dao.EmailTailDao;
import cn.fan.dao.IndexNameDao;
import cn.fan.dao.TaskGridDao;
import cn.fan.dao.TaskRemindDao;
import cn.fan.pojo.*;
import cn.fan.util.SendmailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class EmailRemindService {
    @Autowired
    EmailTailDao emailTailDao;

    @Autowired
    TaskRemindDao taskRemindDao;

    @Autowired
    IndexNameDao indexNameDao;

    @Autowired
    TaskGridDao taskGridDao;

    public void remind(Index index){
        Date date=new Date();
        List<IndexName> indexNameList=indexNameDao.findByCode(index.getCode());
        String indexName="";
        if(indexNameList.size()>0)
            indexName=indexNameList.get(0).getName();

        List<TaskRemind> taskRemindList=taskRemindDao.findByIndexcodeAndIsnotice(index.getCode(),0);//若已通知则不再进行通知

        for(TaskRemind taskRemind:taskRemindList){
            if((taskRemind.getUpdownflag()==0 && taskRemind.getDwjz()>=index.getDwjz()) ||//跌到预警值（或更低）时通知
                    (taskRemind.getUpdownflag()==1 && taskRemind.getDwjz()<=index.getDwjz())){//涨到预警值（或更高）时通知

                EmailTail emailTail=new EmailTail();

                String theme;
                if(taskRemind.getUpdownflag()==0)
                    theme="【买入通知】"+indexName+"("+index.getCode()+")";
                else
                    theme="【卖出通知】"+indexName+"("+index.getCode()+")";

                emailTail.setEmailtheme(theme);
                emailTail.setAddressee(taskRemind.getAddressee());
                String content=getContent(index,taskRemind,indexName);
                emailTail.setEmailcontent(content);
                sendMail(emailTail);
                if(emailTail.getIssuccess()==1){
                    taskRemind.setIsnotice(1);
                    taskRemind.setRemindtime(date);
                    taskRemindDao.save(taskRemind);

                    //根据网格任务表数据 修改通知任务
                    updateTask(taskRemind,date);
                }
            }
        }
    }

    private String getContent(Index index,TaskRemind taskRemind,String indexName){
        String content="基金【"+indexName+"】单位净值已";
        if(taskRemind.getUpdownflag()==0)
            content+="跌至("+index.getDwjz()+")，" +"等于或低于预期值："+taskRemind.getDwjz()+",当前可进行买入！\n" ;
        else if(taskRemind.getUpdownflag()==1)
            content+="涨至("+index.getDwjz()+")，" +"等于或高预期值："+taskRemind.getDwjz()+",当前可进行卖出！\n" ;

        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");

        String updownflag= taskRemind.getUpdownflag()==0?"跌":"涨";

        content+="\n详细信息如下：" +
                "\n  当前基金信息：" +
                "\n    基金代码：" +index.getCode()+
                "\n      基金名称：" +indexName+
                "\n      截止日期：" +index.getJzrq()+
                "\n      单位净值：" +index.getDwjz()+
                "\n      更新时间：" +sdf.format(index.getGztime())+
                "\n"+
                "\n    任务提醒信息：" +
                "\n      涨跌标志：" +updownflag+
                "\n      单位净值：" +taskRemind.getDwjz()+
                "\n      创建时间：" +sdf.format(taskRemind.getCreatetime())+
                "\n";
        return content;
    }

    public void sendMail(EmailTail emailTail){
        emailTail.setSender(SendmailUtil.getMyEmailAccount());
        emailTail.setSendtime(new Date());
        try {
            SendmailUtil.sendEmail(emailTail.getAddressee(), emailTail.getEmailtheme(), emailTail.getEmailcontent());
            emailTail.setIssuccess(1);
        }catch (Exception e){
            System.out.println("邮件发送失败？？");
            emailTail.setIssuccess(0);
        }
        emailTailDao.save(emailTail);
    }

    //更新网格任务信息，修改（未触发的一条）任务提醒，新增任务提醒
    private void updateTask(TaskRemind taskRemind,Date date){
        TaskGrid taskGrid;
        TaskRemind edittaskRemind;
        if(taskRemind.getUpdownflag()==0) {
            taskGrid = taskGridDao.findFirstByFalltaskid(taskRemind.getId());
            edittaskRemind=taskRemindDao.findFirstById(taskGrid.getRisetaskid());

            if(edittaskRemind.getIsnotice()==1 || null==taskGrid)
                return;


            double tempDwjz=edittaskRemind.getDwjz()/(1+taskGrid.getRatio()/100);
            double dwjz=new BigDecimal(tempDwjz).setScale(4,BigDecimal.ROUND_HALF_DOWN).doubleValue();
            edittaskRemind.setDwjz(dwjz);

            taskGrid.setCurrentgridvalue(taskGrid.getCurrentgridvalue()-1);
        }else {
            taskGrid = taskGridDao.findFirstByRisetaskid(taskRemind.getId());
            edittaskRemind=taskRemindDao.findFirstById(taskGrid.getFalltaskid());

            if(edittaskRemind.getIsnotice()==1 || null==taskGrid)
                return;

            double tempDwjz=edittaskRemind.getDwjz()*(1+taskGrid.getRatio()/100);
            double dwjz=new BigDecimal(tempDwjz).setScale(4,BigDecimal.ROUND_HALF_DOWN).doubleValue();
            edittaskRemind.setDwjz(dwjz);

            taskGrid.setCurrentgridvalue(taskGrid.getCurrentgridvalue()+1);
        }


        edittaskRemind.setCreatetime(date);
        taskRemindDao.save(edittaskRemind);

        TaskRemind addTaskRemind=new TaskRemind();
        addTaskRemind.setIndexcode(taskRemind.getIndexcode());
        addTaskRemind.setUpdownflag(taskRemind.getUpdownflag());

        double tempAdd;
        if(taskRemind.getUpdownflag()==0)
            tempAdd=taskRemind.getDwjz()/(1+taskGrid.getRatio()/100);
        else
            tempAdd=taskRemind.getDwjz()*(1+taskGrid.getRatio()/100);

        double addDwjz=new BigDecimal(tempAdd).setScale(4,BigDecimal.ROUND_HALF_DOWN).doubleValue();
        addTaskRemind.setDwjz(addDwjz);

        addTaskRemind.setAddressee(taskRemind.getAddressee());
        addTaskRemind.setIsnotice(0);
        addTaskRemind.setCreatetime(date);
        taskRemindDao.save(addTaskRemind);

        if(taskRemind.getUpdownflag()==0)
            taskGrid.setFalltaskid(addTaskRemind.getId());
        else
            taskGrid.setRisetaskid(addTaskRemind.getId());

        taskGrid.setUpdatetime(date);
        taskGridDao.save(taskGrid);
    }

}
