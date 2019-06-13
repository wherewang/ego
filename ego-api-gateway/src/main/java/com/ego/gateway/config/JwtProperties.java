package com.ego.gateway.config;

import com.ego.auth.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

/**
 * 〈〉
 *
 * @author coach tam
 * @email 327395128@qq.com
 * @create 2019/6/12
 * @since 1.0.0
 * 〈坚持灵活 灵活坚持〉
 */
@Data
@ConfigurationProperties(prefix = "ego.jwt")
public class JwtProperties {

    private String pubKeyPath;// 公钥路径


    private PublicKey publicKey; // 公钥


    private String cookieName; //cookie名字



    @PostConstruct
    public void init() {
        try {
            publicKey=RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
