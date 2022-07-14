package cn.fan.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "taskgrid")
@JsonIgnoreProperties({ "handler","hibernateLazyInitializer" })
public class TaskGrid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   //ID自增
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIndexcode() {
        return indexcode;
    }

    public void setIndexcode(String indexcode) {
        this.indexcode = indexcode;
    }

    public double getInitdwjz() {
        return initdwjz;
    }

    public void setInitdwjz(double initdwjz) {
        this.initdwjz = initdwjz;
    }

    public double getAmountper() {
        return amountper;
    }

    public void setAmountper(double amountper) {
        this.amountper = amountper;
    }

    public double getRatio() {
        return ratio;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

//    public double getDwjzper() {
//        return dwjzper;
//    }
//
//    public void setDwjzper(double dwjzper) {
//        this.dwjzper = dwjzper;
//    }

    public int getCurrentgridvalue() {
        return currentgridvalue;
    }

    public void setCurrentgridvalue(int currentgridvalue) {
        this.currentgridvalue = currentgridvalue;
    }

    public int getRisetaskid() {
        return risetaskid;
    }

    public void setRisetaskid(int risetaskid) {
        this.risetaskid = risetaskid;
    }

    public int getFalltaskid() {
        return falltaskid;
    }

    public void setFalltaskid(int falltaskid) {
        this.falltaskid = falltaskid;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    private int id;
    private String indexcode;//基金代码
    private double initdwjz;//初始（建仓）单位净值
    private double amountper;//每网金额
    private double ratio;//每网比例
//    private double dwjzper;//每网单位净值
    private int currentgridvalue;//当前网格值
    private int risetaskid;//涨的任务提醒ID
    private int falltaskid;//跌的任务提醒ID
    private Date createtime;//创建时间
    private Date updatetime;//更新时间
}
