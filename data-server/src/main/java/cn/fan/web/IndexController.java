package cn.fan.web;

import cn.fan.pojo.IndexName;
import cn.fan.service.IndexNameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import cn.fan.pojo.Index;
import cn.fan.service.IndexService;

import java.util.List;

@RestController
public class IndexController {
    @Autowired IndexService indexService;
    @Autowired IndexNameService indexNameService;

    @GetMapping("/getCode/{fundcode}")
    public List<Index> get(@PathVariable("fundcode") String fundcode) throws Exception {
//        return indexService.fetch_indexes_from_third_part(fundcode);
        return indexService.get(fundcode);
    }

    //获取当天的数据并刷新redis
    @GetMapping("/freshCode/{fundcode}")
    public List<Index> fresh(@PathVariable("fundcode") String fundcode) throws Exception {
        return indexService.fresh(fundcode);
    }

    @GetMapping("/removeCode/{fundcode}")
    public String remove(@PathVariable("fundcode") String fundcode) throws Exception {
        indexService.remove(fundcode);
        return "remove codes successfully";
    }

    @GetMapping("/getCodeName/{fundcode}")
    public List<IndexName> getName(@PathVariable("fundcode") String fundcode) throws Exception {
        return indexNameService.get(fundcode);
    }

    @GetMapping("/getCodeNames")
    public List<IndexName> getName() throws Exception {
        return indexNameService.getAll();
    }

    @GetMapping("/removeNames")
    public String removeNames() throws Exception {
        indexNameService.removeNames();
        return "清除成功！";
    }

    //获取基金的所有数据
    //https://api.doctorxiong.club/v1/fund/detail?code=000563
//    @GetMapping("/test")
//    public String test() throws Exception {
//        indexService.test();
//        return "test!!";
//    }

    //同步所有基金数据（全量）
    @GetMapping("/synchros")
    public String synchros() throws Exception {
        try{
            indexService.synchros();
            return "同步成功！！";
        }catch (Exception e){
            return "同步失败！！";
        }
    }

    //同步单个基金 （全量）
    @GetMapping("/synchro/{fundcode}")
    public String synchro(@PathVariable("fundcode") String fundcode) throws Exception {
        try{
            String str=indexService.synchro(fundcode);
            return "基金【"+str+"】同步成功！";
        }catch (Exception e){
            return "同步失败！！";
        }
    }

    //新增新的基金（并同步数据）
    @GetMapping("/addIndexCode/{fundcode}/{fundname}")
    public String addIndexCode(@PathVariable("fundcode") String fundcode,@PathVariable("fundname") String fundname) throws Exception {
        List<IndexName> indexNameList=indexNameService.findByCode(fundcode);
        if(0==indexNameList.size()){
            IndexName indexName=indexNameService.add(fundcode, fundname);

            //新增完后需要刷新一下基金代码名称(redis)
            indexNameService.removeNames();
            List<IndexName> indexNames=indexNameService.getAll();
            for(IndexName indexName1:indexNames){
                indexNameService.fresh(indexName1.getCode());
            }
            indexService.synchro(fundcode);
            return "基金【"+fundname+"】添加成功，且数据已完成同步！";
        }else{
            return "【"+fundcode+"】基金（名称："+indexNameList.get(0).getName()+"）已存在，不允许重复添加！";
        }
    }

}

