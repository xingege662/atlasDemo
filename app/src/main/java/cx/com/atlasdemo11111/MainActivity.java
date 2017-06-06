package cx.com.atlasdemo11111;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.taobao.android.ActivityGroupDelegate;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private ViewGroup framelayout;
    private Button btn;
    private ActivityGroupDelegate activityGroupDelegate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this,new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
        activityGroupDelegate = new ActivityGroupDelegate(this,savedInstanceState);
        initView();
        setOnclick();
    }

    private void initView() {
        framelayout = (ViewGroup) findViewById(R.id.frameLayout);
        btn = (Button) findViewById(R.id.btn);
        switchToActivity("home","com.taobao.firstbundle.FirstBundleActivity");
    }

    private void setOnclick() {
        btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn:
                Intent intent = new Intent(this, SecondActvity.class);
                startActivity(intent);
                break;
        }
    }
    public void switchToActivity(String key,String activityName){
        Intent intent = new Intent();
        intent.setClassName(getBaseContext(),activityName);
        activityGroupDelegate.startChildActivity(framelayout,key,intent);
    }
}
