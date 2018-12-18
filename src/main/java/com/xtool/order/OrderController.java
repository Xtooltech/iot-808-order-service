package com.xtool.order;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.xtool.enterprise.RespState;
import com.xtool.enterprise.data.ComparePatterns;
import com.xtool.enterprise.data.DataSearchResult;
import com.xtool.enterprise.data.FieldCondition;
import com.xtool.iot808data.devstat.devstatCondition;
import com.xtool.iot808data.devstat.devstatModel;
import com.xtool.iot808data.inorder.inorderModel;
import com.xtool.iot808data.order.EnableOrderMaintainer;
import com.xtool.iot808data.order.orderCondition;
import com.xtool.iot808data.order.orderMaintainer;
import com.xtool.iot808data.order.orderModel;
import com.xtool.service.IDevStatServiceClient;
import com.xtool.service.IInOrderServiceClient;
//import com.xtool.order.remote.IDevStatServiceClient;
//import com.xtool.order.remote.IInOrderServiceClient;
//import com.xtool.order.remote.IOrderIdServiceClient;
import com.xtool.service.IOrderIdServiceClient;

@RestController
@EnableOrderMaintainer
public class OrderController {

	@Autowired
	IOrderIdServiceClient orderIdServiceClient;
	@Autowired
	IInOrderServiceClient inorderServiceClient;
	@Autowired
	IDevStatServiceClient devstatServiceClient;
	
	@Autowired
	orderMaintainer orderMaintainer;
	
	/**
	 * 开始订单。
	 * @param data 订单数据。
	 * @return 操作成功返回true，否则返回false。
	 */
	@RequestMapping(path="/order/begin",method=RequestMethod.POST)
	public RespState<Boolean> begin(@RequestBody orderModel data) {
		RespState<Boolean> result=new RespState<>();
		if(data==null|| StringUtils.isEmpty(data.sno) ||StringUtils.isEmpty(data.uid) || StringUtils.isEmpty(data.appid)) {
			result.setCode(406);
			result.setMsg("Not acceptable");
			result.setData(false);
		}else {
			RespState<String> orderId= orderIdServiceClient.NextId();
			if(orderId.getCode()!=0) {
				result.setCode(407);
				result.setMsg("Not acceptable,no order id");
				result.setData(false);
			}else {
				data.oid=orderId.getData();
				if(data.btime==null)data.btime=new Date();
				if(StringUtils.isEmpty(data.stat))data.stat="S";
				if( orderMaintainer.add(data)) {
					result.setCode(0);
					result.setMsg("ok");
					result.setData(true);
					//在线订单统计，要加上在线订单统计服务。
					//do here.
					
					inorderModel model=new inorderModel();
					model.appid=data.appid;
					model.btime=data.btime;
					model.oid=data.oid;
					model.otype=data.otype;
					model.sno=data.sno;
					model.uid=data.uid;
					//还需要设备状态中读取当前最新的经纬度信息，后面再加入。
					devstatModel devstat=getDeviceState(data.sno);
					if(devstat!=null) {
						model.lng=devstat.lng;
						model.lat=devstat.lat;
					}
					inorderServiceClient.add(model);
				}else {
					result.setCode(501);
					result.setMsg("failed");
					result.setData(false);
				}
			}
		}
		return result;
	}
	/**
	 * 查询订单。
	 * @param condition 查询条件。
	 * @return 查询结果。
	 */
	@RequestMapping(value="/order/get",method= {RequestMethod.POST})
	public RespState<DataSearchResult<orderModel>> get(@RequestBody orderCondition condition){
		if(orderMaintainer==null)return null;
		if(condition==null) {
			condition=new orderCondition();
			condition.setPageIndex(1);
			condition.setPageSize(1);
		}
		//dataMaintainer.mongoTemplate=new MongoTemplate(new MongoClient("19.87.22.3",27017), "p808");
		RespState<DataSearchResult<orderModel>> result=new RespState<DataSearchResult<orderModel>>();
		try {
			DataSearchResult<orderModel> data= orderMaintainer.search(condition);
			result.setData(data);
			result.setCode(0);
			result.setMsg("");
		}catch(Exception ex) {
			result.setData(null);
			result.setCode(500);
			result.setMsg(ex.getMessage());
		}
		return result;
	}

	/**
	 * 结束订单。
	 * @param oid 订单编号。
	 * @param stat 订单状态。
	 * @param desc 描述。
	 * @return 操作成功返回true，否则返回false。
	 */
	@RequestMapping(path="/order/end",method= {RequestMethod.POST,RequestMethod.GET})
	public RespState<Boolean> end(
			@RequestParam 
			String oid
			,@RequestParam 
			String stat
			,@RequestParam(required=false)
			String desc){
		RespState<Boolean> result=new RespState<Boolean>();
		if(StringUtils.isEmpty(oid)||StringUtils.isEmpty(stat)) {
			result.setCode(406);
			result.setMsg("Not acceptable");
			result.setData(false);
		}else {
			try {
				orderModel data=new orderModel();
				data.oid=oid;
				data.stat=stat;
				data.desc=desc;
				if(orderMaintainer.update(data, true)) {
					result.setCode(0);
					result.setMsg("ok");
					result.setData(true);
					//在线订单统计，删除在线订单数据。
					//do here
					inorderServiceClient.remove(oid);
				}
			}catch (Exception e) {
				result.setCode(500);
				result.setMsg(e.getMessage());
				result.setData(false);
			}
		}
		return result;
	}

	private devstatModel getDeviceState(String sno) {
		if(!StringUtils.isEmpty(sno)) {
			devstatCondition condition=new devstatCondition();
			condition.setPageIndex(1);
			condition.setPageSize(1);
			FieldCondition<String> fcSno=new FieldCondition<>();
			fcSno.setComparePattern(ComparePatterns.EQ);
			fcSno.setField("sno");
			fcSno.setValues(new String[] {sno});
			condition.setSno(fcSno);
			RespState<DataSearchResult<devstatModel>> data=devstatServiceClient.get(condition);
			if(data.getCode()!=0)return null;
			if(data.getData()==null || data.getData().total==0)return null;
			return data.getData().getData().get(0);
		}
		return null;
	}
}
