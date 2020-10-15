package com.mr.test;

import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.utils.JwtUtils;
import com.baidu.shop.utils.RsaUtils;
import org.junit.*;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @ClassName JwtTokenTest
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/10/15
 * @Version V1.0
 **/
public class JwtTokenTest {

    //公钥位置
    private static final String pubKeyPath = "E:\\rea.pub";
    //私钥位置
    private static final String priKeyPath = "E:\\rea.pri";
    //公钥对象
    private PublicKey publicKey;
    //私钥对象
    private PrivateKey privateKey;


    /**
     * 生成公钥私钥 根据密文
     * @throws Exception
     */
    @Test
    public void genRsaKey() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "mingrui");
    }


    /**
     * 从文件中读取公钥私钥
     * @throws Exception
     */
    @Before
    public void getKeyByRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    /**
     * 根据用户信息结合私钥生成token
     * @throws Exception
     */
    @Test
    public void genToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(1, "wanghao"), privateKey, 2);
        System.out.println("user-token = " + token);
    }


    /**
     * 结合公钥解析token
     * @throws Exception
     */
    @Test
    public void parseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MSwidXNlcm5hbWUiOiJ3YW5naGFvIiwiZXhwIjoxNjAyNzMzMzMzfQ.C2LFq4ZahsFfjQbIx_HgE8J4jQdNPiK2zYk2WTCd2rMsec2bRDLX8xh9SpCi4TiUvaEmzQcZHg3KFoUaT4USWdwHleJwa4fGJKRs2y64-qI61zRUgx23Bl8k7V_qOhGqFH_AuMULRuotMmko9WW_SyC64ghAH0G2MOoPwnZThN4";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}
