package fu.alfie.com.adplayerexdemo;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ryane.banner_lib.AdPageInfo;
import com.ryane.banner_lib.AdPlayBanner;
import com.ryane.banner_lib.transformer.FadeInFadeOutTransformer;
import com.ryane.banner_lib.view.TitleView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.ryane.banner_lib.AdPlayBanner.ImageLoaderType.GLIDE;
import static com.ryane.banner_lib.AdPlayBanner.IndicatorType.NUMBER_INDICATOR;
import static com.ryane.banner_lib.AdPlayBanner.IndicatorType.POINT_INDICATOR;
import static com.ryane.banner_lib.view.TitleView.Gravity.PARENT_TOP;

public class AdPagerActivity extends AppCompatActivity {

    private AdPlayBanner mAdPlayBanner;
    private ArrayList<AdInfo> adInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_pager);
        //建立取得廣告連線
        connectToAdUrl();
        //取得廣告元件
        mAdPlayBanner = (AdPlayBanner) findViewById(R.id.game_banner);
    }


    private void connectToAdUrl() {
        new OkHttpClient()
            .newCall(new Request.Builder()
            .url("http://192.168.0.34/act/queryAd")
            .build())
            .enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String json = response.body().string();
                    try {
                        String queryAdInfojson = new JSONObject(json).getJSONObject("data").getJSONArray("queryAdInfo").toString();
                        Log.i("JSON", queryAdInfojson);
                        adInfoList = new Gson().fromJson(queryAdInfojson, new TypeToken<ArrayList<AdInfo>>(){}.getType());
                        Log.i("JSON", adInfoList.size()+"/"+ adInfoList.get(0).getName());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showAdPager(adInfoList);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("JSON", "Nodata");
                    }
                }
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("OKHTTP", "Connection fail");
                }
        });
    }

    public void showAdPager(ArrayList<AdInfo> list) {
        List<AdPageInfo> mDatas = new ArrayList<>();
        for(int i = 0; i < list.size(); i++){
            String imgPath = list.get(i).getImg();
            if (!imgPath.contains("http")){
                imgPath = "http://192.168.0.34/"+list.get(i).getImg();
            }

            mDatas.add(new AdPageInfo(list.get(i).getName(), imgPath, list.get(i).getLink(), i));
        }
        mAdPlayBanner
                .setImageLoadType(GLIDE)
                .setImageViewScaleType(AdPlayBanner.ScaleType.FIT_CENTER)
                .setOnPageClickListener(new AdPlayBanner.OnPageClickListener() {
                    @Override
                    public void onPageClick(AdPageInfo info, int postion) {
                        if (info.getClickUlr() != null){
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(info.getClickUlr())));
                        }
                        Toast.makeText(getApplicationContext(), "圖片標題:" + info.getTitle() + "\n連結網址:" + info.getClickUlr() + "\n位置:" + postion +"\n優先等級:" + info.getOrder(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setAutoPlay(true)
                .setIndicatorType(NUMBER_INDICATOR)
                .setNumberViewColor(0xcc00A600, 0xccea0000, 0xffffffff)
                .setInterval(3000)
                .addTitleView(new TitleView(this).setPosition(PARENT_TOP).setTitlePadding(5, 5, 5, 5).setTitleMargin(0, 0, 0, 25).setTitleSize(16).setViewBackground(0x55000000).setTitleColor(getResources().getColor(R.color.white)))
                .setBannerBackground(0xff000000)
                .setPageTransfromer(new FadeInFadeOutTransformer())
                .setInfoList((ArrayList<AdPageInfo>) mDatas)
                .setUp();
    }

    public void onCloseBottonClick(View view) {
        finish();
    }
}
