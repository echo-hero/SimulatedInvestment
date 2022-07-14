package cn.fan.web;

import cn.fan.pojo.IndexPerson;
import cn.fan.service.IndexPersonService;
import cn.fan.util.Page4Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class PersInvestController {

    @Autowired
    IndexPersonService indexPersonService;

    @GetMapping("/getPersonById/{id}")
    @CrossOrigin
    public IndexPerson getByid(@PathVariable("id") int id) throws Exception {
        return indexPersonService.geyById(id);
    }

    @GetMapping("/getPerson/{fundcode}")
    @CrossOrigin
    public List<IndexPerson> get(@PathVariable("fundcode") String fundcode) throws Exception {
        return indexPersonService.get(fundcode);
    }

    @GetMapping("/getPerson/{fundcode}/{start}")
    @CrossOrigin
    public Page4Util<IndexPerson> getPage(@PathVariable("fundcode") String fundcode, @PathVariable("start") int start) throws Exception {
        List<IndexPerson> indexPersonList=indexPersonService.get(fundcode);
        if(indexPersonList.size()==0)
            return null;
        int size=5;//每页显示的数量
        Page4Util<IndexPerson> page4Util=new Page4Util<>(start,size,5,"indexPerson");//默认前端显示5页
        page4Util.page(indexPersonList);
        return page4Util;
    }

    @PostMapping("/addPerson")
    @CrossOrigin
    public String add(@RequestBody IndexPerson indexPerson){
        try{
            String str=indexPersonService.addIndexPerson(indexPerson);
            return str;
        }catch (Exception e){
            return "新增失败！";
        }
    }

    @PostMapping("/editPerson")
    @CrossOrigin
    public String editPerson(@RequestBody IndexPerson indexPerson){
        return indexPersonService.editPerson(indexPerson);
    }

    @GetMapping("/overview")
    @CrossOrigin
    public List<Map<String,Object>> overview(){
        List<Map<String,Object>> result=indexPersonService.getOverview();
        return result;
    }
}
