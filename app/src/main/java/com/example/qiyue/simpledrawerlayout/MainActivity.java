package com.example.qiyue.simpledrawerlayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.nineoldandroids.view.ViewHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SimpleDragLayout simpleDragLayout = (SimpleDragLayout) findViewById(R.id.dl);
        simpleDragLayout.setDragListener(new SimpleDragLayout.DragListener() {
            //界面打开的时候
            @Override
            public void onOpen() {
            }
            //界面关闭的时候
            @Override
            public void onClose() {
            }

            //界面滑动的时候
            @Override
            public void onDrag(float percent) {
             //   ViewHelper.setAlpha(iv_icon, 1 - percent);
            }
        });

        ListView lv = (ListView) findViewById(R.id.listView);
        lv.setAdapter(new ArrayAdapter<String>(MainActivity.this,
                R.layout.item_text, new String[] { "item 01", "item 02",
                "item 03", "item 04", "item 05", "item 06",
                "item 07", "item 08", "item 09", "item 10",
                "item 11", "item 12", "item 13", "item 14",
                "item 15", "item 16", "item 17",
                "item 18", "item 19", "item 20", "item 21"}));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                Toast.makeText(MainActivity.this,"Click Item "+position,Toast.LENGTH_SHORT).show();
            }
        });
    }
}
