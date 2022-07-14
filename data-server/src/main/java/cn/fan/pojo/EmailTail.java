package cn.fan.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "emailtail")
@JsonIgnoreProperties({ "handler","hibernateLazyInitializer" })
public class EmailTail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   //ID自增
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Date getSendtime() {
        return sendtime;
    }

    public void setSendtime(Date sendtime) {
        this.sendtime = sendtime;
    }

    public String getEmailtheme() {
        return emailtheme;
    }

    public void setEmailtheme(String emailtheme) {
        this.emailtheme = emailtheme;
    }

    public String getAddressee() {
        return addressee;
    }

    public void setAddressee(String addressee) {
        this.addressee = addressee;
    }

    public String getEmailcontent() {
        return emailcontent;
    }

    public void setEmailcontent(String emailcontent) {
        this.emailcontent = emailcontent;
    }

    public int getIssuccess() {
        return issuccess;
    }

    public void setIssuccess(int issuccess) {
        this.issuccess = issuccess;
    }

    private int id;//主键
    private String sender;//发件人
    private Date sendtime;//发送时间
    private String emailtheme;//邮件主题
    private String addressee;//收件人
    private String emailcontent;//邮件内容
    private int issuccess;//是否成功
}
