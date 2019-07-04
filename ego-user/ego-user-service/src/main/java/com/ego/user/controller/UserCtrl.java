package com.ego.user.controller;

import com.ego.user.pojo.Address;
import com.ego.user.pojo.User;
import com.ego.user.pojo.Zone;
import com.ego.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class UserCtrl {

    @Autowired
    private UserService userService;

//    Request URL: http://api.ego.com/api/user/check/18398369432/2
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> check(@PathVariable("data") String data,
                                      @PathVariable("type") Integer type)
    {
        Boolean isOk = userService.check(data,type);
        if(isOk==null)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(!isOk);
    }

    @PostMapping("/send")
    public ResponseEntity<Boolean> send(@RequestParam("phone") String phone)
    {
        Boolean isOk = userService.send(phone);

        return ResponseEntity.ok(isOk);
    }

    @PostMapping("/register")
    public ResponseEntity<Boolean> register(@Valid User user, @RequestParam("code") String code)
    {
            Boolean isOk = userService.register(user,code);
            if(isOk==null)
            {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据用户名和密码，查询用户
     */
    @GetMapping("/query")
    public ResponseEntity<User> queryUser(@RequestParam("username")String username,
                                      @RequestParam("password") String password)
    {
        User user = userService.queryUser(username,password);
        if(user == null)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(user);
    }

    /**
     * 根据用户名id，查询用户所有地址
     */
    @GetMapping("/address")
    public ResponseEntity<List<Address>> queryAddress(@RequestParam("id")Long uid)
    {
        List<Address> addressList = userService.queryAddressByUid(uid);
        if(addressList == null || addressList.size()==0)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(addressList);
    }

    /**
     * 根据用户名id，添加用户新地址
     */
    @PostMapping("/insertAddress")
    public ResponseEntity<Boolean> insertAddress(@RequestBody Address address)
    {
        Boolean isOk = userService.insertAddress(address);
        if(isOk==null)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
       return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/findCurrAddr")    /*多余的方法*/
    public ResponseEntity<Address> findCurrAddr(@RequestParam("id") Long id)
    {
//        Long.parseLong()   Long.valueOf(id)
        Address address = userService.findCurrAddrById(id);
        if(address == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(address);
    }

    /**
     * 根据用户名id，添加用户新地址
     */
    @PostMapping("/updateAddress")
    public ResponseEntity<Boolean> updateAddress(@RequestBody Address address)
    {
        Boolean isOk = userService.updateAddress(address);
        if(isOk==null)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

//    @DeleteMapping("/deleteAddress")
//    public ResponseEntity<Boolean> deleteAddress(@RequestParam("id") Long id)
//    {
//        Boolean result = userService.deleteAddressById(id);
//        if(result == false)
//        {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        }
//        return ResponseEntity.ok(result);
//    }

    @DeleteMapping("/deleteAddress/{id}")
    public ResponseEntity<Boolean> deleteAddress(@PathVariable("id") Long id)
    {
        Boolean result = userService.deleteAddressById(id);
        if(result == false)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/selectProvince/{id}")
    public ResponseEntity<List<Zone>> selectProvince(@PathVariable("id") Long id)
    {
        List<Zone> provinceList = userService.findProvinceById(id);
        if(provinceList == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(provinceList);
    }

    @GetMapping("/selectCity/{id}")
    public ResponseEntity<List<Zone>> selectCity(@PathVariable("id") Long id)
    {
        List<Zone> cityList = userService.findCityById(id);
        if(cityList == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(cityList);
    }

    @GetMapping("/selectCounty/{id}")
    public ResponseEntity<List<Zone>> selectCounty(@PathVariable("id") Long id)
    {
        List<Zone> countyList = userService.findCountyById(id);
        if(countyList == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(countyList);
    }
}
