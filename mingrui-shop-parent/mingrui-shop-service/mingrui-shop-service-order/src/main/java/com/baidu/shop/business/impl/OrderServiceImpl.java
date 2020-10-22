package com.baidu.shop.business.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.business.OrderService;
import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.dto.CarDTO;
import com.baidu.shop.dto.OrderDTO;
import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.entity.OrderDetailEntity;
import com.baidu.shop.entity.OrderEntity;
import com.baidu.shop.entity.OrderStatusEntity;
import com.baidu.shop.mapper.OrderDetailMapper;
import com.baidu.shop.mapper.OrderMapper;
import com.baidu.shop.mapper.OrderStatusMapper;
import com.baidu.shop.redis.repository.RedisRepository;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.IdWorker;
import com.baidu.shop.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName OrderServiceImpl
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/10/21
 * @Version V1.0
 **/
@RestController
public class OrderServiceImpl extends BaseApiService implements OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private OrderDetailMapper orderDetailMapper;

    @Resource
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private JwtConfig jwtConfig;

    @Resource
    private IdWorker idWorker;

    @Resource
    private RedisRepository redisRepository;

    public static String GOODS_CAR_PRE = "goods_car_";

    @Override
    public Result<String> createOrder(OrderDTO orderDTO, String token) {
        long orderId = idWorker.nextId();
        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtConfig.getPublicKey());
            OrderEntity orderEntity = new OrderEntity();
            Date date = new Date();
            orderEntity.setUserId(userInfo.getId() + "");
            orderEntity.setOrderId(orderId);
            orderEntity.setPaymentType(orderDTO.getPayType());
            orderEntity.setSourceType(1);
            orderEntity.setInvoiceType(1);
            orderEntity.setCreateTime(date);
            orderEntity.setBuyerRate(1);
            orderEntity.setBuyerNick(userInfo.getUsername());
            orderEntity.setBuyerMessage("wanghao");

            List<Long> longs = Arrays.asList(0L);
            List<OrderDetailEntity> orderDetailList = Arrays.asList(orderDTO.getSkuIds().split(",")).stream().map(skuIdSkr -> {
                CarDTO car = redisRepository.getHash(GOODS_CAR_PRE + userInfo.getId(), skuIdSkr, CarDTO.class);
                if (car == null) throw new RuntimeException("数据异常");
                OrderDetailEntity orderDetailEntity = new OrderDetailEntity();
                orderDetailEntity.setImage(car.getImage());
                orderDetailEntity.setNum(car.getNum());
                orderDetailEntity.setOwnSpec(car.getOwnSpec());
                orderDetailEntity.setPrice(car.getPrice());
                orderDetailEntity.setSkuId(car.getSkuId());
                orderDetailEntity.setTitle(car.getTitle());
                orderDetailEntity.setOrderId(orderId);

                longs.set(0, car.getPrice() * car.getNum() + longs.get(0));

                return orderDetailEntity;
            }).collect(Collectors.toList());

            orderEntity.setActualPay(longs.get(0));
            orderEntity.setTotalPay(longs.get(0));

            OrderStatusEntity orderStatusEntity = new OrderStatusEntity();
            orderStatusEntity.setCreateTime(date);
            orderStatusEntity.setOrderId(orderId);
            orderStatusEntity.setStatus(1);

            orderMapper.insertSelective(orderEntity);
            orderDetailMapper.insertList(orderDetailList);
            orderStatusMapper.insertSelective(orderStatusEntity);

            delRedisCar(orderDTO, userInfo);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return this.setResult(HTTPStatus.OK,"", orderId + "");
    }

    @Transactional
    public void delRedisCar(OrderDTO orderDTO, UserInfo userInfo) {
        Arrays.asList(orderDTO.getSkuIds().split(",")).stream().forEach(skuidStr -> {
            redisRepository.delHash(GOODS_CAR_PRE + userInfo.getId(),skuidStr);
        });
    }
}
