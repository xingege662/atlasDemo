package com.taobao.firstbundle;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.taobao.firstbundle.fragment.BlankFragment;

public class FirstBundleActivity extends AppCompatActivity implements BlankFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstbundle);
    }

//    @Override
//    public void onListFragmentInteraction(DummyContent.DummyItem item) {
//
//    }


    @Override
    protected void onResume() {
        super.onResume();
      //Toast.makeText(this, "1.0.2吧1111111111", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "单模块部署1111111", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
