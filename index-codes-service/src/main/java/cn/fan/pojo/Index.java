package cn.fan.pojo;

import java.io.Serializable;//可序列化
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "indexmain")
@JsonIgnoreProperties({ "handler","hibernateLazyInitializer" })
public class Index implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   //ID自增
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) { this.id = id;}

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getJzrq() { return jzrq; }

    public void setJzrq(String jzrq) {
        this.jzrq = jzrq;
    }

    public double getDwjz() {
        return dwjz;
    }

    public void setDwjz(double dwjz) {
        this.dwjz = dwjz;
    }

    public Date getGztime() {
        return gztime;
    }

    public void setGztime(Date gztime) {
        this.gztime = gztime;
    }

    private int id;//主键
    private String code;//基金代码
    private String jzrq;//截至日期
    private double dwjz;//单位净值
    private Date gztime;//更新时间

}
