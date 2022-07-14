package cn.fan.service;

import cn.fan.dao.IndexPersonDao;
import cn.fan.pojo.Index;
import cn.fan.pojo.IndexName;
import cn.fan.pojo.IndexPerson;
import cn.fan.util.SpringContextUtil;
import cn.hutool.core.collection.CollUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@CacheConfig(cacheNames="indexperson")
public class IndexPersonService {

    @Autowired RestTemplate restTemplate;

    @Autowired IndexService indexService;

    @Autowired IndexPersonDao indexPersonDao;

//    @Autowired IndexPersonMapper indexPersonMapper;

    private Map<String,List<IndexPerson>> stringListMap=new HashMap<>();

    @Cacheable(key="'indexPerson-code-'+ #p0")
    public List<IndexPerson> get(String code){
        if(null==code) return null;
        if(CollUtil.toList().size()==0)//无数据时，使用刷新数据方式进行获取
            return fresh(code);
        else
            return CollUtil.toList();
    }

    @CacheEvict(key="'indexPerson-code-'+ #p0")
    public void remove(String code){    }

    @Cacheable(key="'indexPerson-code-'+ #p0")
    public List<IndexPerson> store(String code){
        return stringListMap.get(code);
    }

    public List<IndexPerson> fresh(String code){
        IndexPersonService indexPersonService= SpringContextUtil.getBean(IndexPersonService.class);
        indexPersonService.remove(code);
        List<IndexPerson> indexPersonList=indexPersonDao.findByCode(code);
        stringListMap.put(code,indexPersonList);
        return store(code);
    }

    //新增
    public String addIndexPerson(IndexPerson indexPerson) throws Exception{
        if(indexPersonDao.findByCodeAndOperatetime(indexPerson.getCode(),indexPerson.getOperatetime()).size()==0){
            //给空值的数据赋值 判断值是否合理
            if(indexPerson.getDwjz()==0){
                List<Index> indexList=indexService.findByCodeAndJzrq(indexPerson.getCode(),indexPerson.getOperatetime());
                if(indexList.size()>0){
                    double dwjz=indexList.get(0).getDwjz();
                    indexPerson.setDwjz(dwjz);
                }else{
                    return "未获取到该天的单位净值数据！";
                }
            }

            if(indexPerson.getShare()==0){
                double share=indexPerson.getSummoney()/(1+indexPerson.getCommission()/100)/indexPerson.getDwjz();
                indexPerson.setShare(share);
            }else if(indexPerson.getSummoney()==0){
                double summoney=indexPerson.getDwjz()*indexPerson.getShare()*(1+indexPerson.getCommission()/100);
                indexPerson.setSummoney(summoney);
            }

//            System.out.println("person:"+indexPerson.getCode()+","+indexPerson.getOperatetime()+","+indexPerson.getBuysellflag()+","+indexPerson.getCommission()+","
//                    +indexPerson.getDwjz()+","+indexPerson.getShare()+".");

            indexPersonDao.save(indexPerson);
            fresh(indexPerson.getCode());
            return "添加成功！";
        }else{
            return "当天已购买该基金，不允许新增，可进行修改处理！";
        }
    }

    //修改
    public String editPerson(IndexPerson indexPerson){
        if(null==geyById(indexPerson.getId()))
            return "该条数据不存在，请刷新后重试！";
        indexPersonDao.save(indexPerson);
        fresh(indexPerson.getCode());
        return "已修改成功！";
    }

    public IndexPerson geyById(int id){
        return indexPersonDao.findById(id).get();
    }

    //总投资一览
    public List<Map<String,Object>> getOverview(){
        List<Map<String,Object>> result=new ArrayList<>();
        double indexSum_sumMoney=0;
        double indexSum_holdSum=0;
        double indexSum_profit=0;

        List<IndexName> indexNameList=getNames();
        for(IndexName indexName:indexNameList){
            Map<String,Object> map=new HashMap<>();
            double sumMoney=0;//投资总金额*
            double sellMoney=0;
            double holdSum=0;//持有总金额*
            double shareSum=0;//总投资份额*
            double shellShare=0;//卖出的份额
            double holdShare=0;//持有份额*
            double buyAVG=0;//买入均价*
            double holdAVG=0;//持有均价*
            double nowDwjz=0;//市值*
            double profit=0;//总亏盈*

            Date now=new Date();
            SimpleDateFormat sdf1 =new SimpleDateFormat("yyyy-MM-dd" );
            Index index=indexService.firstByCodeAndJzrq(indexName.getCode(),sdf1.format(now));
            nowDwjz=index.getDwjz();

            List<IndexPerson> indexPersonList=indexPersonDao.findByCode(indexName.getCode());
            if(indexPersonList.size()==0)
                continue;
            for(IndexPerson indexPerson:indexPersonList){
                if(indexPerson.getBuysellflag()==0){
                    sumMoney+=indexPerson.getSummoney();
                    shareSum+=indexPerson.getShare();

                }else{
                    shellShare+=indexPerson.getShare();
                    sellMoney+=indexPerson.getSummoney();
                }
            }
            holdShare=shareSum-shellShare;
            holdSum=holdShare*nowDwjz;
            profit=holdSum-sumMoney+sellMoney;
            buyAVG=sumMoney/shareSum;
            holdAVG=(sumMoney-sellMoney)/holdShare;

            map.put("indexName",indexName);
            map.put("sumMoney",sumMoney);
            map.put("holdSum",holdSum);
            map.put("shareSum",shareSum);
            map.put("holdShare",holdShare);
            map.put("buyAVG",buyAVG);
            map.put("holdAVG",holdAVG);
            map.put("nowDwjz",nowDwjz);
            map.put("profit",profit);
            result.add(map);

            indexSum_sumMoney+=sumMoney;
            indexSum_holdSum+=holdSum;
            indexSum_profit+=profit;
        }

        Map<String,Object> indexSum=new HashMap<>();
        IndexName nameSum=new IndexName();
        nameSum.setCode("");
        nameSum.setName("汇总");
        indexSum.put("indexName",nameSum);
        indexSum.put("sumMoney",indexSum_sumMoney);
        indexSum.put("holdSum",indexSum_holdSum);
        indexSum.put("shareSum","");
        indexSum.put("holdShare","");
        indexSum.put("buyAVG","");
        indexSum.put("holdAVG","");
        indexSum.put("nowDwjz","");
        indexSum.put("profit",indexSum_profit);
        result.add(indexSum);

        return result;
    }


    private List<IndexName> getNames(){
        List<Map> temp= (List<Map>)restTemplate.getForObject("http://127.0.0.1:8031/api-codes/names",Map.class).get("indexName");
        List<IndexName> indexNameList=new ArrayList<>();
        for(Map tem:temp){
            IndexName indexName=new IndexName();
            indexName.setId(Integer.parseInt(tem.get("id").toString()));
            indexName.setCode(tem.get("code").toString());
            indexName.setName(tem.get("name").toString());
            indexNameList.add(indexName);
        }
        return indexNameList;
    }

}
