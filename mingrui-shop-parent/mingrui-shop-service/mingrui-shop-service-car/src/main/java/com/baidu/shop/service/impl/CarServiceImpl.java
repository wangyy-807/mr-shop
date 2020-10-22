package com.baidu.shop.service.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.service.CarService;
import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.dto.CarDTO;
import com.baidu.shop.dto.SpecParamDTO;
import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.entities.SkuEntity;
import com.baidu.shop.entities.SpecParamEntity;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.feign.SpecificationFeign;
import com.baidu.shop.redis.repository.RedisRepository;
import com.baidu.shop.utils.JSONUtil;
import com.baidu.shop.utils.JwtUtils;
import com.baidu.shop.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName CarServiceImpl
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/10/19
 * @Version V1.0
 **/
@RestController
@Slf4j
public class CarServiceImpl extends BaseApiService implements CarService {

    @Autowired
    private RedisRepository redisRepository;

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private GoodsFeign goodsFeign;

    @Autowired
    private SpecificationFeign specificationFeign;

    public static String GOODS_CAR_PRE = "goods_car_";

    @Override
    public Result<JSONObject> addCar(CarDTO carDTO, String token) {

        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtConfig.getPublicKey());

            CarDTO redisCar = redisRepository.getHash(GOODS_CAR_PRE + userInfo.getId(), carDTO.getSkuId() + "", CarDTO.class);
            CarDTO saveCar = null;
            log.debug("通过key : {} ,skuid : {} 获取到的数据为 : {}",GOODS_CAR_PRE + userInfo.getId(),carDTO.getSkuId(),redisCar);
            if (null == redisCar){
                Result<SkuEntity> skuResult = goodsFeign.getSkuBySkuId(carDTO.getSkuId() + "");
                if (skuResult.getCode() == 200){
                    SkuEntity data = skuResult.getData();
                    carDTO.setTitle(data.getTitle());
                    carDTO.setPrice(Long.valueOf(data.getPrice()));
                    carDTO.setImage(StringUtil.isNotEmpty(data.getImages()) ? data.getImages().split(",")[0] : "");
                    carDTO.setUserId(userInfo.getId());

                    Map<String, Object> specMap = JSONUtil.toMap(data.getOwnSpec());
                    Map<String, Object> map = new HashMap<>();
                    specMap.forEach((key,value) -> {
                        SpecParamDTO specParamDTO = new SpecParamDTO();
                        specParamDTO.setId(Integer.valueOf(key));
                        Result<List<SpecParamEntity>> specParam = specificationFeign.getSpecParam(specParamDTO);
                        if (specParam.getData().size() == 1){
                            key = specParam.getData().get(0).getName();
                        }
                        map.put(key,value);
                    });
                    String specStr = JSONUtil.toJsonString(map);

                    carDTO.setOwnSpec(specStr);
                    saveCar = carDTO;
                    log.debug("新增商品到购物车redis,KEY : {} , skuid : {} , car : {}",GOODS_CAR_PRE + userInfo.getId(),carDTO.getSkuId(),JSONUtil.toJsonString(carDTO));
                }
            }else{
                redisCar.setNum(carDTO.getNum() + redisCar.getNum());
                saveCar = redisCar;
                log.debug("当前用户购物车中有将要新增的商品，重新设置num : {}" , redisCar.getNum());
            }
            redisRepository.setHash(GOODS_CAR_PRE + userInfo.getId()
                    ,carDTO.getSkuId() + "", JSONUtil.toJsonString(saveCar));
            log.debug("新增到redis数据成功");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> mergeCar(String clientCarList, String token) {

        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(clientCarList);
        List<CarDTO> carList = com.alibaba.fastjson.JSONObject.parseArray(jsonObject.getJSONArray("clientCarList").toJSONString(), CarDTO.class);

        carList.stream().forEach(car -> addCar(car, token));//遍历新增到购物车

        return this.setResultSuccess();
    }

    @Override
    public Result<List<CarDTO>> getUserGoodsCar(String token) {

        List<CarDTO> carDTOS = new ArrayList<>();
        UserInfo infoFromToken = null;
        try {
            infoFromToken = JwtUtils.getInfoFromToken(token, jwtConfig.getPublicKey());
            Map<String, String> hash = redisRepository.getHash(GOODS_CAR_PRE + infoFromToken.getId());
            hash.forEach((key,value) -> {
                carDTOS.add(JSONUtil.toBean(value,CarDTO.class));
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this.setResultSuccess(carDTOS);
    }

    @Override
    public Result<List<CarDTO>> getCurrentUserGoodsCar(String token) {

        List<CarDTO> carList = new ArrayList<>();
        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtConfig.getPublicKey());

            Map<String, String> map = redisRepository.getHash(GOODS_CAR_PRE + userInfo.getId());

            map.forEach((key,value) -> {
                carList.add(JSONUtil.toBean(value,CarDTO.class));
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this.setResultSuccess(carList);
    }

    @Override
    public Result<JSONObject> carNumUpdate(Long skuId, Integer type, String token) {

        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtConfig.getPublicKey());
            CarDTO car = redisRepository.getHash(GOODS_CAR_PRE + userInfo.getId(), skuId + "", CarDTO.class);
            if (null != car){
                car.setNum(type == 1 ? car.getNum() + 1 : car.getNum() - 1);
                redisRepository.setHash(GOODS_CAR_PRE + userInfo.getId(), skuId + "", JSONUtil.toJsonString(car));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this.setResultSuccess();
    }
}
