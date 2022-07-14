package cn.fan.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "indexperson")
@JsonIgnoreProperties({ "handler","hibernateLazyInitializer" })
public class IndexPerson implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   //ID自增
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getBuysellflag() {
        return buysellflag;
    }

    public void setBuysellflag(int buysellflag) {
        this.buysellflag = buysellflag;
    }

    public String getOperatetime() {
        return operatetime;
    }

    public void setOperatetime(String operatetime) {
        this.operatetime = operatetime;
    }

    public double getShare() {
        return share;
    }

    public void setShare(double share) {
        this.share = share;
    }

    public double getDwjz() {
        return dwjz;
    }

    public void setDwjz(double dwjz) {
        this.dwjz = dwjz;
    }

    public double getCommission() {
        return commission;
    }

    public void setCommission(double commission) {
        this.commission = commission;
    }

    public double getSummoney() {
        return summoney;
    }

    public void setSummoney(double summoney) {
        this.summoney = summoney;
    }

    private int id;//主键
    private String code;//基金代码
    private int buysellflag;//买卖标准 0:买 1:卖
    private String operatetime;//买入日期
    private double share;//买入份额
    private double dwjz;//买入单位净值
    private double commission;//手续费
    private double summoney;//(买卖)总金额

//    private String buytime;//买入日期
//    private double buyshare;//买入份额
//    private double buydwjz;//买入单位净值
//    private double commission;//手续费
//    private String selltime;//卖出日期
//    private double sellshare;//卖出份额
//    private double selldwjz;//卖出单位净值
//    private double profit;//盈利
}
