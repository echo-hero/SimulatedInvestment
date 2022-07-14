package cn.fan.service;

import java.util.*;

import cn.fan.pojo.MoveAvg;
import cn.fan.pojo.Trade;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import cn.fan.pojo.Index;
import cn.hutool.core.collection.CollUtil;

@Service
@CacheConfig(cacheNames="indexes")
public class IndexService {
    private List<Index> indexes;

    @Cacheable(key="'indexData-code-'+ #p0")
    public List<Index> get(String fundcode){
        Index index = new Index();
        index.setCode("000000");
        return CollUtil.toList(index);
    }

    //获取（日期）区间内的最大值、最小值，及移动平均线
    public Map<String,Object> moveAvgList(List<Index> indexList, int ma,double buyRate,double sellRate){
        double init=indexList.get(0).getDwjz();//初始金额

        double initCash = 1000;//本金
        double cash = initCash;//当前金额
        double share = 0;//当前份额

        //盈利次数、盈利率、亏损次数、亏损率
        int winCount = 0;
        double totalWinRate = 0;
        int lossCount = 0;
        double totalLossRate = 0;
        //平均盈利（亏损）率
        double avgWinRate = 0;
        double avgLossRate = 0;

        List<MoveAvg> moveAvgList=new ArrayList<>();//移动平均线集合
        List<MoveAvg> profitList=new ArrayList<>();//收益率集合
        List<Trade> tradeList=new ArrayList<>();//记录交易集合
        double[] mals=new double[ma];//存储ma天的单位净值数据

        for(int i=0;i<indexList.size();i++){
            double nowDwjz=indexList.get(i).getDwjz();
            mals[i%ma]=nowDwjz;

            //平均线
            double avg=0;//平均值
            int m=0;//平均的天数
            double maSum=0;
            for(double d:mals){
                if(d!=0){
                    m++;
                    maSum+=d;
                }
            }
            if(m!=0)
                avg=maSum/m;

            MoveAvg moveAvg=new MoveAvg();//移动平均线
            moveAvg.setDate(indexList.get(i).getJzrq());
            moveAvg.setValue(avg);
            moveAvgList.add(moveAvg);


            //利润线
            MoveAvg profit=new MoveAvg();
            profit.setDate(indexList.get(i).getJzrq());

            if(i-1<ma){
                profit.setValue(nowDwjz);
            }else{
                double sectionMax= Arrays.stream(mals).max().getAsDouble();//ma区间内的最大值
                double increase_rate = nowDwjz/avg;
                double decrease_rate = nowDwjz/sectionMax;

                if(increase_rate>buyRate){//超过买入比例
                    if(0==share){//且还未买入，则购买
                        share=cash/nowDwjz;
                        cash=0;

                        //新增一条交易记录
                        tradeList.add(getTradeByIndex(indexList.get(i)));
                    }
                }else if(decrease_rate<sellRate){//低于卖点
                    if(0!=share){//且未卖，则出售
                        cash=nowDwjz*share;
                        share=0;

                        //修改交易记录的出售信息
                        Trade trade=tradeList.get(tradeList.size()-1);
                        trade.setSellDate(indexList.get(i).getJzrq());
                        trade.setSellClosePoint(nowDwjz);
                        trade.setRate(cash/initCash);

                        //计算交易统计相关数据
                        if(trade.getSellClosePoint()-trade.getBuyClosePoint()>0) {
                            totalWinRate +=(trade.getSellClosePoint()-trade.getBuyClosePoint())/trade.getBuyClosePoint();
                            winCount++;
                        }
                        else {
                            totalLossRate +=(trade.getSellClosePoint()-trade.getBuyClosePoint())/trade.getBuyClosePoint();
                            lossCount ++;
                        }
                    }
                }
            }
            double value=0;//目前的所有金额（买入的份额按照当日金额转换）
            if(share!=0)
                value=nowDwjz*share;
            else
                value=cash;
            double rate=value/initCash;//当前盈利率

            profit.setValue(rate*init);
            profitList.add(profit);
        }

        avgWinRate = totalWinRate / winCount;
        avgLossRate = totalLossRate / lossCount;

        Map<String,Object> result=new HashMap<>();
        result.put("moveAvgList",moveAvgList);
        result.put("profitList",profitList);
        result.put("tradeList", tradeList);

        result.put("winCount", winCount);
        result.put("lossCount", lossCount);
        result.put("avgWinRate", avgWinRate);
        result.put("avgLossRate", avgLossRate);

        return result;
    }

    //网格交易 基金集合、网格宽度（波动率）、初始(建仓)金额、初始份额
    public List<Trade> grid(List<Index> dateIndexs,double gridWidth,double initAmount,double initShare){
        double lastAmount=initAmount;//最后一次买入金额网格线
        double waveAmount=initAmount*gridWidth;//波动金额

        double highestDwjz=initAmount+waveAmount*(initShare+1);
        System.out.println("highestDwjz:"+highestDwjz);

        List<Trade> tradeList=new ArrayList<>();//交易集合

        //建仓
        int build=0;//建仓的天数
        for(int i=0;i<dateIndexs.size();i++){
            if(dateIndexs.get(i).getDwjz()<=lastAmount){
                build=i+1;
                for(int j=0;j<initShare;j++){
                    Trade trade=getTradeByIndex(dateIndexs.get(i));
                    tradeList.add(trade);
                }
                break;
            }
        }

        //网格交易
        for(int i=build;i<dateIndexs.size();i++){
            Index index=dateIndexs.get(i);
            if(index.getDwjz()<=lastAmount-waveAmount){//买
                Trade trade=getTradeByIndex(dateIndexs.get(i));
                tradeList.add(trade);
                lastAmount-=waveAmount;
            }else if(index.getDwjz()>=lastAmount+waveAmount && index.getDwjz()<highestDwjz){//卖(金额不能大于网格顶点)
                Trade trade=getUnsold(tradeList);
                if(null!=trade) {
                    trade.setSellDate(index.getJzrq());
                    trade.setSellClosePoint(index.getDwjz());
                    trade.setRate(index.getDwjz() / trade.getBuyClosePoint() - 1);
                    lastAmount+=waveAmount;
                }
            }
        }

        //未交易的网格按照最后一天的金额计算收益（卖出）
        SellAllGrid(tradeList,dateIndexs.get(dateIndexs.size()-1));

        return tradeList;
    }

    //网格统计
    public Map<String,Object> gridStat(List<Trade> gridList,double perGridAmount){
        Map<String,Object> result=new HashMap<>();

        int gridCount=gridList.size();//交易次数
        int completeCount=0;//完成次数
        double completeProfit=0;//完成的收益
        double profitCount=0;//总收益
        double completeRate=0;//完成的收益率
        double rateProfit=0;//总收益率
        double rateAnnual=0;//年化收益率

        double complCost=0;//完成的成本
        double costSum=0;//总成本
        for(Trade trade:gridList){
            double share=perGridAmount/trade.getBuyClosePoint();//份额
            if(trade.getRate()>0){
                completeCount++;
                completeProfit+=(trade.getSellClosePoint()-trade.getBuyClosePoint())*share;
                complCost+=trade.getBuyClosePoint()*share;
            }
            profitCount+=(trade.getSellClosePoint()-trade.getBuyClosePoint())*share;
            costSum+=trade.getBuyClosePoint()*share;
        }

        if(complCost!=0)
            completeRate=completeProfit/complCost;
        if(costSum!=0)
            rateProfit=profitCount/costSum;

        result.put("gridCount",gridCount);
        result.put("completeCount",completeCount);
        result.put("completeProfit",completeProfit);
        result.put("profitCount",profitCount);
        result.put("completeRate",completeRate);
        result.put("rateProfit",rateProfit);
        return result;
    }

    //定投
    public Map<String,Object> castSurely(List<Index> dateIndexs,double cycle,double money){
        Map<String,Object> result=new HashMap<>();

        int number=0;//定投次数
        double shareSum=0;//总份额
        for(int i=0;i<dateIndexs.size();i+=cycle){
            shareSum += money/dateIndexs.get(i).getDwjz();
            number++;
        }

        double nowDwjz=dateIndexs.get(dateIndexs.size()-1).getDwjz();
        double profit=number*money-shareSum*nowDwjz;

        result.put("number",number);
        result.put("shareSum",shareSum);
        result.put("nowDwjz",nowDwjz);
        result.put("money",money);
        result.put("profit",profit);
        return result;
    }

    //新增交易记录
    private static Trade getTradeByIndex(Index index){
        Trade trade=new Trade();
        trade.setBuyDate(index.getJzrq());
        trade.setBuyClosePoint(index.getDwjz());
        trade.setSellDate("n/a");
        trade.setSellClosePoint(0);
        return trade;
    }

    //获取区间年数（包含非工作日）
    public double getYear(List<Index> allIndexDatas) {
        double years;
        String sDateStart = allIndexDatas.get(0).getJzrq();
        String sDateEnd = allIndexDatas.get(allIndexDatas.size()-1).getJzrq();
        Date dateStart = DateUtil.parse(sDateStart);
        Date dateEnd = DateUtil.parse(sDateEnd);
        long days = DateUtil.between(dateStart, dateEnd, DateUnit.DAY);
        years = days/365f;
        return years;
    }

    //获取最新一个未卖出的交易记录
    private static Trade getUnsold(List<Trade> tradeList){
        Trade trade=null;
        for(int i=tradeList.size()-1;i>=0;i--){
            if(tradeList.get(i).getSellClosePoint()==0){
                trade=tradeList.get(i);
                break;
            }
        }
        return trade;
    }

    //未交易的网格按照最后一天的金额计算收益（卖出）
    private static void SellAllGrid(List<Trade> tradeList,Index lastIndex){
        for(Trade trade:tradeList){
            if(trade.getSellClosePoint()==0){
                trade.setSellClosePoint(lastIndex.getDwjz());
                trade.setSellDate(lastIndex.getJzrq());
                trade.setRate(lastIndex.getDwjz() / trade.getBuyClosePoint() - 1);
            }
        }
    }
}
