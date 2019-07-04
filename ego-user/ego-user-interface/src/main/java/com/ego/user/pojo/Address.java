package com.ego.user.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Table(name = "tb_address")
public class Address {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;
    private Long uid;
    private String name;
    private String address;
    private String telphone;
    private String email;
    private String addrAlias;
    private String zone;
    private Boolean isDefault; // 是否为默认地址，0非默认，1默认
    private Date createTime;// 创建时间
    private Date lastUpdateTime;// 最后修改时间

}
