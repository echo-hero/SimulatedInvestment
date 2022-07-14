package cn.fan.service;

import cn.fan.dao.IndexDao;
import cn.fan.dao.IndexNameDao;
import cn.fan.mapper.IndexMapper;
import cn.fan.pojo.Index;
import cn.fan.pojo.IndexName;
import cn.fan.util.SpringContextUtil;

import java.io.*;
import java.util.*;

import cn.hutool.core.collection.CollUtil;
import org.springframework.beans.factory.annotation.Autowired;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import cn.hutool.core.collection.CollectionUtil;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@CacheConfig(cacheNames="indexes")
public class IndexService {
//    private List<Index> indexe;
    private Map<String, List<Index>> indexDatas=new HashMap<>();
    @Autowired RestTemplate restTemplate;

    @Autowired IndexDao indexDao;

    @Autowired private IndexNameDao indexNameDao;

    @Autowired IndexMapper indexMapper;

    //获取近几天的数据（备用接口近有一天的数据）
    @HystrixCommand(fallbackMethod = "third_part_not_connected")
    @Cacheable(key="indexData-code-'+ #p0")
    public List<Index> fetch_indexes_from_third_part(String fundcode){
        String urlStr="https://fundf10.eastmoney.com/F10DataApi.aspx?type=lsjz&code="+fundcode;
        String data=getUrlStr(urlStr).replace("</td>","@</td>").replace("</th>","@</th>");

        //去除HTML格式
        String regEx_html = "<[^>]+>";
        Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(data);
        data = m_html.replaceAll("");

        if(data.contentEquals("暂无数据") || data.length()<180){
            return spare(fundcode);
        }else{
            int start=data.indexOf("分红送配")+5;
            int end=data.indexOf("records");
            String temp=data.substring(start,end);
            return map2IndexMain(temp,fundcode);
        }
    }

    //备用接口，只能获取最新一条数据
    private List<Index> spare(String fundcode){
        String urlStr="http://fundgz.1234567.com.cn/js/"+fundcode+".js?rt=1463558676006";
        String data=getUrlStr(urlStr);
        //如果该接口无数据，则使用备用接口
        if(data.length()<60){
            return null;
        }
        String temp=data.substring(9,data.indexOf("});"));
        return map2Index(temp);
    }

    //刷新数据
    //@HystrixCommand(fallbackMethod = "third_part_not_connected")
    public List<Index> fresh(String fundcode) {
        List<Index> indexList =fetch_indexes_from_third_part(fundcode);
        addSJK(indexList);

        List<Index> indices=indexMapper.findByCode(fundcode);
        indexDatas.put(fundcode, indices);

        IndexService indexDataService = SpringContextUtil.getBean(IndexService.class);
        indexDataService.remove(fundcode);

        return indexDataService.store(fundcode);
    }

    //清空缓存（redis）
    @CacheEvict(key="'indexData-code-'+ #p0")
    public void remove(String fundcode){    }

    //保存数据
    @Cacheable(key="'indexData-code-'+ #p0")
    public List<Index> store(String code){
        return indexDatas.get(code);
    }

    //获取数据
    @Cacheable(key="'indexData-code-'+ #p0")
    public List<Index> get(String fundcode){
        if(CollUtil.toList().size()==0)//无数据时，使用刷新数据方式进行获取
            return fresh(fundcode);
        else
            return CollUtil.toList();
    }

    //第三方未返回时执行该方法
    public List<Index> third_part_not_connected(String fundcode){
        System.out.println("third_part_not_connected()");
        Index index= new Index();
        index.setCode("000000");
        return CollectionUtil.toList(index);
    }

//    public void test() throws Exception{
//        String src = "C:\\Users\\A\\Desktop\\1\\1.txt";
//        File file = new File(src);
//        FileReader fileReader = new FileReader(file);
//        BufferedReader br = new BufferedReader(fileReader);
//        StringBuilder sb = new StringBuilder();
//        String temp = "";
//        while ((temp = br.readLine()) != null) {
//            sb.append(temp + "\n");
//        }
//        br.close();
//        String js = sb.toString();
//
//        String[] tempList=js.split(" ");
//
//        Index index=new Index();
//        int tem=tempList.length/5;
//        System.out.println("tem:"+tem);
//        for(int i=0;i<tempList.length-4;i+=5){
//            System.out.println("#####:"+tempList[i]+","+tempList[i+1]+","+tempList[i+2]+","+tempList[i+3]+","+tempList[i+4]);
//            indexMapper.insertIndex(tempList[i],tempList[i+1],tempList[i+2],tempList[i+3],tempList[i+4]);
//        }
//    }

    //同步所有基金数据
    public void synchros() throws Exception{
        List<IndexName> indexNames=indexNameDao.findAll();
        for(IndexName indexName:indexNames){
            synchro(indexName.getCode());
        }
    }

    //同步单个基金
    public String synchro(String code){
        List<IndexName> indexNameList=indexNameDao.findByCode(code);
        if(0==indexNameList.size()){
            return "未添加该基金，请先进行新增操作！";
        }else{
            IndexName indexName=indexNameList.get(0);
            String urlStr="https://api.doctorxiong.club/v1/fund/detail?code="+code;

            String data=getUrlStr(urlStr);
            System.out.println("data:"+data);

            int start=data.indexOf("\"netWorthData\"");
            int end=data.indexOf("\"totalNetWorthData\"");

            String str1=data.substring(start+16,end-3);
            System.out.println("str1:"+str1);
            doctorxiong(str1,code);
            fresh(code);
            return "基金【"+indexName.getName()+"】已同步成功！";
        }
    }

    private static String getUrlStr(String urlStr){
        String data="";
        try{
            URL url=new URL(urlStr);

            try {
                InputStream is = url.openStream();
                InputStreamReader isr = new InputStreamReader(is, "utf-8");

                BufferedReader br = new BufferedReader(isr);
                data = br.readLine();

                br.close();
                isr.close();
                is.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }catch(MalformedURLException e){
            e.printStackTrace();
        }
        return data;
    }

    private List<Index> map2Index(String temp) {
        List<Index> indexList=new ArrayList<>();
        String[] tempList=temp.split("\"");
        String[] temps=new String[7];
        for(int i=1;i<tempList.length;i++){
            if(i%4==3){
                temps[i/4]=tempList[i];
            }
        }
        Index index= new Index();
        index.setCode(temps[0]);
        index.setJzrq(temps[2]);
        try{
            index.setDwjz(Double.parseDouble(temps[3]));
            index.setGztime(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(temps[6]));
        }catch(Exception e){
            e.printStackTrace();
        }
        indexList.add(index);
        return indexList;
    }

    //主接口的数据转换
    private List<Index> map2IndexMain(String temp,String fundcode) {
        List<Index> indexList=new ArrayList<>();
        String[] tempList=temp.split("@");
        for(int i=0;i+1<tempList.length;i+=7){
            Index index=new Index();
            index.setCode(fundcode);
            index.setJzrq(tempList[i]);
            index.setGztime(new Date());
            try{
                index.setDwjz(Double.parseDouble(tempList[i+1]));
            }catch(Exception e){ e.printStackTrace(); }
            indexList.add(index);
        }
        return indexList;
    }

    //将数据插入到数据库中
    private void addSJK(List<Index> indexList){
        try{
            for(Index index:indexList){
                if(index.getCode().equals("000000"))
                    continue;
                if(indexMapper.findByCodeAndJzrq(index.getCode(),index.getJzrq()).isEmpty()){
                    indexDao.save(index);

                    EmailRemindService emailRemindService = SpringContextUtil.getBean(EmailRemindService.class);
                    emailRemindService.remind(index);
                }
            }
        }catch (Exception e){e.printStackTrace();}
    }

    //将接口的数据转换成list数组,未入库的添加到数据库中
    private void doctorxiong(String str,String code){
        String[] netWorths=str.split("\"");
        for(int i=0;i+3<netWorths.length;i+=8){
            String jzrq=netWorths[i+1];
            List<Index> indexList=indexMapper.findByCodeAndJzrq(code,jzrq);

            if(indexList.size()==0){
                Index index=new Index();
                index.setCode(code);
                index.setJzrq(jzrq);
                try{
                    index.setDwjz(Double.parseDouble(netWorths[i+3]));
                }catch (Exception e){
                    index.setDwjz(0);//无法转换的赋值0
                }
                index.setGztime(new Date());
                indexDao.save(index);
            }
        }
    }

}
