package cn.fan.web;

import java.util.*;

import cn.fan.config.IpConfiguration;
import cn.fan.pojo.*;
import cn.fan.service.IndexNameService;
import cn.fan.service.TaskRemindService;
import cn.fan.util.Page4Util;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import cn.fan.service.IndexService;

@RestController
public class IndexController {
    @Autowired IndexService indexService;
    @Autowired IndexNameService indexNameService;
    @Autowired IpConfiguration ipConfiguration;
    @Autowired TaskRemindService taskRemindService;
//通过redis获取数据
//	http://127.0.0.1:8011/codes

    //获取基金数据
    @GetMapping("/code/{fundcode}")
    @CrossOrigin
    public Map<String,Object> codes(@PathVariable("fundcode") String fundcode) throws Exception {
        System.out.println("current instance's port is "+ ipConfiguration.getPort());
        Map<String,Object> result=new HashMap<>();
        result.put("code",indexService.get(fundcode));
        return result;
    }

    //获取基金名称
    @GetMapping("/name/{fundcode}")
    @CrossOrigin
    public Map<String,Object> name(@PathVariable("fundcode") String fundcode) throws Exception {
        System.out.println("name current instance's port is "+ ipConfiguration.getPort());
        Map<String,Object> result=new HashMap<>();
        result.put("name",indexNameService.get(fundcode));
        return result;
    }

    //获取所有基金名称
    @GetMapping("/names")
    @CrossOrigin
    public Map<String,Object> names() throws Exception {
        System.out.println("name current instance's port is "+ ipConfiguration.getPort());
        Map<String,Object> result=new HashMap<>();
        result.put("indexName",indexNameService.getAll());
        return result;
    }

    //获取所有基金名称
    @GetMapping("/names/{start}")
    @CrossOrigin
    public Page4Util<IndexName> names(@PathVariable("start") String start) throws Exception {
        System.out.println("name current instance's port is "+ ipConfiguration.getPort());
        int startInt;
        try{
            startInt=Integer.parseInt(start);
        }catch(Exception e){
            startInt=0;
        }

        List<IndexName> indexNameList= indexNameService.getAll();

        int size=5;//每页显示的数量
        Page4Util<IndexName> page4Util=new Page4Util<>(startInt,size,5,"indexName");//默认前端显示5页
        page4Util.page(indexNameList);
        return  page4Util;
    }

    @GetMapping("/code/{fundcode}/{startDate}/{endDate}/{ma}/{buyRate}/{sellRate}")
    @CrossOrigin
    public Map<String,Object> dateCodes(@PathVariable("fundcode") String fundcode
                                        ,@PathVariable("startDate") String strStartDate
                                        ,@PathVariable("endDate") String strEndDate
                                        ,@PathVariable("ma") int ma
                                        ,@PathVariable("buyRate") double buyRate
                                        ,@PathVariable("sellRate") double sellRate) throws Exception {
        System.out.println("current instance's port is "+ ipConfiguration.getPort());
        Map<String,Object> result=new HashMap<>();

        List<Index> indexList=indexService.get(fundcode);
        String indexStartDate = indexList.get(0).getJzrq();
        String indexEndDate = indexList.get(indexList.size()-1).getJzrq();
        List<Index> dateIndexs=filterByDateRange(indexList,strStartDate,strEndDate);

        Map<String,Object> simulate=indexService.moveAvgList(dateIndexs,ma,buyRate,sellRate);
        List<MoveAvg> moveAvgList=(List<MoveAvg>)simulate.get("moveAvgList");
        List<MoveAvg> profitList=(List<MoveAvg>)simulate.get("profitList");
        List<Trade> tradeList=(List<Trade>)simulate.get("tradeList");

        double years=indexService.getYear(dateIndexs);
        double indexIncomeTotal = (dateIndexs.get(dateIndexs.size()-1).getDwjz() - dateIndexs.get(0).getDwjz()) / dateIndexs.get(0).getDwjz();
        double indexIncomeAnnual = (double) Math.pow(1+indexIncomeTotal, 1/years) - 1;
        double trendIncomeTotal = (profitList.get(profitList.size()-1).getValue() - profitList.get(0).getValue()) / profitList.get(0).getValue();
        double trendIncomeAnnual = (double) Math.pow(1+trendIncomeTotal, 1/years) - 1;

        int winCount = (Integer) simulate.get("winCount");
        int lossCount = (Integer) simulate.get("lossCount");
        double avgWinRate = (double) simulate.get("avgWinRate");
        double avgLossRate = (double) simulate.get("avgLossRate");


        result.put("dateCodes",dateIndexs);
        result.put("indexStartDate", indexStartDate);
        result.put("indexEndDate", indexEndDate);
        result.put("moveAvgList", moveAvgList);
        result.put("profitList", profitList);
        result.put("tradeList", tradeList);
        result.put("years", years);
        result.put("indexIncomeTotal", indexIncomeTotal);
        result.put("indexIncomeAnnual", indexIncomeAnnual);
        result.put("trendIncomeTotal", trendIncomeTotal);
        result.put("trendIncomeAnnual", trendIncomeAnnual);

        result.put("winCount", winCount);
        result.put("lossCount", lossCount);
        result.put("avgWinRate", avgWinRate);
        result.put("avgLossRate", avgLossRate);
        return result;
    }

    //网格交易  code/基金代码/开始日期/结束日期/网格宽度/初始金额/初次买入份额
    @GetMapping("/grid/{fundcode}/{startDate}/{endDate}/{gridWidth}/{initAmount}/{initShare}")
    @CrossOrigin
    public Map<String,Object> dateCodes(@PathVariable("fundcode") String fundcode,
                                        @PathVariable("startDate") String strStartDate,
                                        @PathVariable("endDate") String strEndDate,
                                        @PathVariable("gridWidth") double gridWidth,
                                        @PathVariable("initAmount") double initAmount,
                                        @PathVariable("initShare") double initShare) throws Exception {
        System.out.println("current instance's port is "+ ipConfiguration.getPort());
        Map<String,Object> result=new HashMap<>();

        List<Index> indexList=indexService.get(fundcode);
        List<Index> dateIndexs=filterByDateRange(indexList,strStartDate,strEndDate);

        List<Trade> gridList=indexService.grid(dateIndexs,gridWidth,initAmount,initShare);

        Map<String,Object> gridStats=indexService.gridStat(gridList,1000);//默认网格成功为1000元
        int gridCount=(int)gridStats.get("gridCount");
        int completeCount=(int)gridStats.get("completeCount");
        double completeProfit=(double)gridStats.get("completeProfit");
        double profitCount=(double)gridStats.get("profitCount");
        double completeRate=(double)gridStats.get("completeRate");
        double rateProfit=(double)gridStats.get("rateProfit");

        double years=indexService.getYear(dateIndexs);
        double rateAnnual=Math.pow(1+rateProfit, 1/years) - 1;//年化收益率

        result.put("gridList",gridList);
        result.put("gridCount",gridCount);
        result.put("completeCount",completeCount);
        result.put("completeProfit",completeProfit);
        result.put("profitCount",profitCount);
        result.put("completeRate",completeRate);
        result.put("rateProfit",rateProfit);
        result.put("rateAnnual",rateAnnual);
        return result;
    }

    //定投  code/基金代码/开始日期/结束日期/定投周期/每期金额
    @GetMapping("/castSurely/{fundcode}/{startDate}/{endDate}/{cycle}/{money}")
    @CrossOrigin
    public Map<String,Object> castSurely (@PathVariable("fundcode") String fundcode,
                                       @PathVariable("startDate") String strStartDate,
                                       @PathVariable("endDate") String strEndDate,
                                       @PathVariable("cycle") int cycle,
                                       @PathVariable("money") double money) throws Exception {

        List<Index> indexList=indexService.get(fundcode);
        List<Index> dateIndexs=filterByDateRange(indexList,strStartDate,strEndDate);
        Map<String,Object> result=indexService.castSurely(dateIndexs,cycle,money);
        return result;
    }

    //时间区间内的基金数据
    private List<Index> filterByDateRange(List<Index> indexList,String strStartDate, String strEndDate){
        if(StrUtil.isBlankOrUndefined(strStartDate) || StrUtil.isBlankOrUndefined(strEndDate) )
            return indexList;

        List<Index> result=new ArrayList<>();
        Date startDate = DateUtil.parse(strStartDate);
        Date endDate = DateUtil.parse(strEndDate);

        for(Index index:indexList){
            Date date=DateUtil.parse(index.getJzrq());
            if(date.getTime()>=startDate.getTime() &&
                    date.getTime()<=endDate.getTime()){
                result.add(index);
            }
        }
        return  result;
    }

    @PostMapping("/addTaskRemind")
    @CrossOrigin
    public String addTaskRemind(@RequestBody TaskRemind taskRemind){
        try {
            String result=taskRemindService.addTaskRemind(taskRemind);
            //taskRemindService.fresh(taskRemind.getIndexcode());
            return result;
        }catch (Exception e){
            return "新增失败！";
        }
    }

    @GetMapping("/getTaskRemind/{updownflag}/{isnotice}/{start}")
    @CrossOrigin
    public Page4Util<TaskRemind> getTaskRemind(@PathVariable("updownflag") int updownflag,
                                               @PathVariable("isnotice") int isnotice,
                                               @PathVariable("start") int start){

        List<TaskRemind> taskRemindList=taskRemindService.getTaskRemind(updownflag,isnotice);

        int size=5;//每页显示的数量
        int navigatePages=5;//最多显示5个页面
        Page4Util<TaskRemind> page4Util=new Page4Util<>(start,size,navigatePages,"taskRemind");
        if(taskRemindList.size()>0)
            page4Util.page(taskRemindList);
        else
            page4Util.setContent(null);

        return page4Util;
    }

    @GetMapping("/getTask/{id}")
    @CrossOrigin
    public TaskRemind getTask(@PathVariable("id") int id){
        return taskRemindService.getTaskRemindById(id);
    }

    @PostMapping("/editTask")
    @CrossOrigin
    public String editTask(@RequestBody TaskRemind taskRemind){
        return taskRemindService.editTask(taskRemind);
    }
}

