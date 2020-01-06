package com.graduation.project.snakegame;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;


public class DifficultyActivity extends Activity implements OnClickListener{
    private SharedPreferences saved;
    private SharedPreferences.Editor editor;

    RadioButton button_jiandan,button_yiban,button_kunnan;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty);
        saved = PreferenceManager.getDefaultSharedPreferences(this);
        int level = saved.getInt("nandu",500);

        button_jiandan = (RadioButton) findViewById(R.id.button_difficulty1);
        button_yiban = (RadioButton) findViewById(R.id.button_difficulty2);
        button_kunnan = (RadioButton) findViewById(R.id.button_difficulty3);

        button_jiandan.setOnClickListener(this);
        button_yiban.setOnClickListener(this);
        button_kunnan.setOnClickListener(this);
    }
    @Override
    public void onClick(View v){
        editor=saved.edit();
        switch(v.getId()){
            case R.id.button_difficulty1:
                if(button_jiandan.isChecked()){
                    editor.putInt("nandu",500);
                }
                break;
            case R.id.button_difficulty2:
                if(button_yiban.isChecked()){
                    editor.putInt("nandu",200);
                }
                break;
            case R.id.button_difficulty3:
                if(button_kunnan.isChecked()){
                    editor.putInt("nandu",100);
                }
                break;
        }
        editor.commit();
    }
}
