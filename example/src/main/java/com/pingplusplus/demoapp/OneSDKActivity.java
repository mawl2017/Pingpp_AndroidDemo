package com.pingplusplus.demoapp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.pingplusplus.android.PingppLog;
import com.pingplusplus.libone.PaymentHandler;
import com.pingplusplus.libone.PingppOne;

public class OneSDKActivity extends FragmentActivity
    implements OnClickListener {

//  private static String URL = "http://218.244.151.190/demo/charge";
  private static String URL = "http://120.26.225.230:819/index.php/Api/Charge/recharge";

  private ListView mListView;
  private GoodsAdapter myAdapter;
  private List<Good> mList;
  private TextView amountView,tv_number;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_yi_shou_kuan);

    mListView = (ListView) findViewById(R.id.listView);
    amountView = (TextView) findViewById(R.id.textview_amount);
    tv_number= (TextView) findViewById(R.id.textView2);
    mList = new ArrayList<Good>();

    mList.add(new Good("橡胶花盆", R.drawable.icon, 1, 0.02f));
    mList.add(new Good("搪瓷水壶", R.drawable.icon2, 1, 0.03f));
    mList.add(new Good("扫把和簸箕", R.drawable.icon3, 1, 0.05f));

    calculate();
    myAdapter = new GoodsAdapter(this, mList);
    mListView.setAdapter(myAdapter);
    findViewById(R.id.button).setOnClickListener(this);
    myAdapter.registerDataSetObserver(new DataSetObserver() {
      @Override public void onChanged() {
        calculate();
        super.onChanged();
      }
    });

    //设置需要使用的支付方式
    PingppOne.enableChannels(new String[] { "wx", "alipay", "upacp", "bfb", "jdpay_wap" });

    // 提交数据的格式，默认格式为json
    // PingppOne.CONTENT_TYPE = "application/x-www-form-urlencoded";
    PingppOne.CONTENT_TYPE = "application/json";

    PingppLog.DEBUG = true;
  }

  private void calculate() {
    //总价格
    float amount = 0;
    int number = 0;
    for (Good good : mList) {
      amount += good.getPrice() * good.getCount();
      number += good.getCount();
    }
    amountView.setText(String.format("%.2f", amount));
    tv_number.setText("总共"+number+"件商品");
  }

  @Override public void onClick(View v) {
    // 产生个订单号
    String orderNo = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());

    // 计算总金额（以分为单位）
    int amount = 0;
    JSONArray billList = new JSONArray();
    for (Good good : mList) {
      amount += good.getPrice() * good.getCount() * 100;
      billList.put(good.getName() + " x " + good.getCount());
    }
    // 构建账单json对象
    JSONObject bill = new JSONObject();

    // 自定义的额外信息 选填
    JSONObject extras = new JSONObject();
    try {
      extras.put("extra1", "extra1");
      extras.put("extra2", "extra2");
    } catch (JSONException e) {
      e.printStackTrace();
    }

    try {
      bill.put("order_no", orderNo);
      bill.put("amount", amount);
      bill.put("extras", extras);
    } catch (JSONException e) {
      e.printStackTrace();
    }

    //壹收款: 创建支付通道的对话框
    PingppOne.showPaymentChannels(this, bill.toString(), URL, new PaymentHandler() {
      @Override public void handlePaymentResult(Intent data) {
        if (data != null) {
          /**
           * code：支付结果码  -2:服务端错误、 -1：失败、 0：取消、1：成功
           * error_msg：支付结果信息
           */
          int code = data.getExtras().getInt("code");
          String result = data.getExtras().getString("result");
          String mesge = null;
          if(code==-2){
            mesge="服务端错误";
          }else if(code==-1){
            mesge="支付失败";
          }else if(code==0){
            mesge="支付取消";
          }else if(code==1){
            mesge="支付成功";
          }
          Log.d("info","支付结果==="+mesge);
        }
      }
    });
  }
}
