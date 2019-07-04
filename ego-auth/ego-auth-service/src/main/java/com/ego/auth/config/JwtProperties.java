package com.ego.auth.config;

import com.ego.auth.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@ConfigurationProperties(prefix = "ego.jwt")
public class JwtProperties {
    private String secret; // 密钥

    private String pubKeyPath;// 公钥路径

    private String priKeyPath;// 私钥路径

    private int expire;// token过期时间

    private PublicKey publicKey; // 公钥

    private PrivateKey privateKey; // 私钥

    private String cookieName; //cookie名字

    private Integer cookieMaxAge; //cookie有效期 <秒>

    public JwtProperties() {
        System.out.println();//在执行构造方法时，上面属性还没有注入值
    }
    //初始化方法，该方法在构造方法执行后，再执行的
    //从java对象的生命周期来看，虚拟机里面先new对象时，里面的成员变量先默认初始化，再显示初始化
    //再执行构造方法，最后spring才帮你把属性值注入进来
    @PostConstruct
    public void init() {
        //如果没有就初始化公钥和私钥
        File pubKeyFile = new File(pubKeyPath);
        File priKeyFile = new File(priKeyPath);

        if(!pubKeyFile.exists() || !priKeyFile.exists())
        {
            try {
                RsaUtils.generateKey(pubKeyPath, priKeyPath, secret);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            publicKey=RsaUtils.getPublicKey(pubKeyPath);
            privateKey=RsaUtils.getPrivateKey(priKeyPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
