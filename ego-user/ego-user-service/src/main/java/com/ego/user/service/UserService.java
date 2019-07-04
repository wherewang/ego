package com.ego.user.service;

import com.ego.common.utils.CodecUtils;
import com.ego.common.utils.NumberUtils;
import com.ego.user.mapper.AddressMapper;
import com.ego.user.mapper.UserMapper;
import com.ego.user.mapper.ZoneMapper;
import com.ego.user.pojo.Address;
import com.ego.user.pojo.User;
import com.ego.user.pojo.Zone;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
     private UserMapper userMapper;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private ZoneMapper zoneMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final String prefix = "user:sms:code";
    /**
     * 验证数据是否存在
     * @param data  验证的数据（用户名或手机号）
     * @param type 1，验证用户名  2.验证手机号
     * @return
     */
    public Boolean check(String data, Integer type){
        Boolean result = true;
        if(type!=null)
        {
            User user = new User();

            if(type==1)
            {
              user.setUsername(data);
            }else if(type==2){
              user.setPhone(data);
            }
            List<User> select = userMapper.select(user);
            if(select==null || select.size()==0){
                return false;
            }
        }
        return result;
    }

    /**
     * 发送验证码短信
     * @param phone
     */
    public Boolean send(String phone) {
        //随机6位验证码
        String code = NumberUtils.generateCode(6);

        Map<String, String> map = new HashMap<>();
        map.put("phone",phone);
        map.put("code", code);
        //通知短信微服务，发送验证码短信
        amqpTemplate.convertAndSend("sms.verify.code",map);

        //将验证码保存到redis
        stringRedisTemplate.opsForValue().set("user:sms:code"+phone,code,5, TimeUnit.MINUTES);
        return true;
    }

    public Boolean register(User user ,String code)
    {
        Boolean result = false;
        //判断验证码是否正确
        if(StringUtils.isNotEmpty(code))
        {
        //到redis中获取当前手机所对应的验证码
            String key = prefix+user.getPhone();
             String redisCode = stringRedisTemplate.opsForValue().get(key);
            if(redisCode!=null && code.endsWith(redisCode))
            {
                //保存“新用户”到数据库
                user.setCreated(new Date());
                //对明文密码进行加密，密文保存
                String encodePassword = CodecUtils.passwordBcryptEncode(user.getUsername(),user.getPassword());
                user.setPassword(encodePassword);

                userMapper.insertSelective(user);
                result = true;

                //注册成功后，redis中的验证码应该失效
                stringRedisTemplate.delete(key);
            }
        }
        return result;
    }


    public User queryUser(String username,String password)
    {
        //去数据库，查询该用户
        User user = new User();
        user.setUsername(username);
        User selectOne = userMapper.selectOne(user);
        //校验该用户
        if(selectOne==null)
        {
            return  null;
        }
        //校验密码
        if(!CodecUtils.passwordConfirm(username+password,selectOne.getPassword()))
        {
            return  null;
        }
        //用户名和密码都正确
        return selectOne;
    }

    public List<Address> queryAddressByUid(Long uid) {
        Address address = new Address();
        address.setUid(uid);
        return addressMapper.select(address);
    }

    public Boolean insertAddress(Address address) {
        Boolean result = false;
        if(address != null)
        {
            address.setCreateTime(new Date());
           addressMapper.insertSelective(address);
                result = true;
        }
        return result;
    }

    public Boolean updateAddress(Address address) {
        Boolean result = false;
        if(address != null)
        {
            address.setLastUpdateTime(new Date());
            addressMapper.updateByPrimaryKeySelective(address);
            result = true;
        }
        return result;
    }

    public Address findCurrAddrById(Long id) {
        return addressMapper.selectByPrimaryKey(id);
    }

    public Boolean deleteAddressById(Long id) {
        int i = addressMapper.deleteByPrimaryKey(id);
        if (i>0)
        {
            return true;
        }
        return false;
    }

    public List<Zone> findProvinceById(Long id) {
        Zone zone = new Zone();
        zone.setParentId(id);
        return zoneMapper.select(zone);
    }

    public List<Zone> findCityById(Long id) {
        Zone zone = new Zone();
        zone.setParentId(id);
        return zoneMapper.select(zone);
    }

    public List<Zone> findCountyById(Long id) {
        Zone zone = new Zone();
        zone.setParentId(id);
        return zoneMapper.select(zone);
    }
}
