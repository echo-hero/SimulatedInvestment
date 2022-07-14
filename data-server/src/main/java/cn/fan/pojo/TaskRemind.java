package cn.fan.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "taskremind")
@JsonIgnoreProperties({ "handler","hibernateLazyInitializer" })
public class TaskRemind implements Serializable {

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

    public int getUpdownflag() {
        return updownflag;
    }

    public void setUpdownflag(int updownflag) {
        this.updownflag = updownflag;
    }

    public double getDwjz() {
        return dwjz;
    }

    public void setDwjz(double dwjz) {
        this.dwjz = dwjz;
    }

    public String getAddressee() {
        return addressee;
    }

    public void setAddressee(String addressee) {
        this.addressee = addressee;
    }

    public int getIsnotice() {
        return isnotice;
    }

    public void setIsnotice(int isnotice) {
        this.isnotice = isnotice;
    }

    public Date getRemindtime() {
        return remindtime;
    }

    public void setRemindtime(Date remindtime) {
        this.remindtime = remindtime;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    private int id;
    private String indexcode;//基金代码
    private int updownflag;//涨跌标志 0:跌 1:涨
    private double dwjz;//单位净值
    private String addressee;//收件人
    private int isnotice;//是否已通知 0:未通知 1:已通知
    private Date remindtime;//通知时间
    private Date createtime;//创建时间
}
