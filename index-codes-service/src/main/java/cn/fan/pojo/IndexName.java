package cn.fan.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "indexname")
@JsonIgnoreProperties({ "handler","hibernateLazyInitializer" })
public class IndexName implements Serializable{
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private int id;//主键
    private String code;//基金代码
    private String name;//基金名称
}
