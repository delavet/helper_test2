package com.example.asus.yh1;

import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.*;

import com.example.asus.yh1.lib.Parameter;
import com.example.asus.yh1.lib.WebConnection;

import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends AppCompatActivity {
    public static final int SHOW_NAME=0;
    public static final int SHOW_ERROR=1;

    private Handler handler=new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case SHOW_NAME:
                {
                    String name=(String)msg.obj;
                    TextView rNameView=(TextView)findViewById(R.id.rNameView);
                    rNameView.setText("你的真名就是"+name);
                }break;
                case SHOW_ERROR:{
                    String errorMsg=(String)msg.obj;
                    Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public void fuc(View v){
        EditText txtUser=(EditText)findViewById(R.id.txtUserName);
        EditText txtPswd=(EditText)findViewById(R.id.txtPswd);
        String userName=txtUser.getText().toString();
        String pswd=txtPswd.getText().toString();

        if(userName==""||pswd==""||userName==null||pswd==null){
            Toast.makeText(getApplicationContext(), "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        doLogin(userName,pswd);

        return;
    }

    private void doLogin(String userName,String pswd){
        final String n=userName;
        final String p=pswd;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    WebConnection.connectWithGet("http://portal.pku.edu.cn/portal2013/index.jsp");
                    ArrayList<Parameter> list=new ArrayList<>();
                    list.add(new Parameter("appid", "portal"));
                    list.add(new Parameter("userName",n));
                    list.add(new Parameter("password", p));
                    list.add(new Parameter("redirUrl",
                            "http://portal.pku.edu.cn/portal2013/login.jsp/../ssoLogin.do"));
                    Parameter rt1=WebConnection.connectWithPost("https://iaaa.pku.edu.cn/iaaa/oauthlogin.do", list);
                    if(!"200".equals(rt1.name)){
                        throw new Exception("登录iaaa失败！");
                    }
                    JSONObject json=new JSONObject(rt1.value);
                    Boolean success=json.optBoolean("success");
                    String token=json.optString("token");
                    Random ran=new Random();
                    WebConnection.connectWithGet("http://portal.pku.edu.cn/portal2013/ssoLogin.do?rand="+ran.nextDouble()+"&token="+token);
                    Parameter rt2=WebConnection.connectWithGet("http://portal.pku.edu.cn/portal2013/isUserLogged.do");
                    if(!"200".equals(rt2.name)){
                        throw new Exception("获取姓名失败！");
                    }
                    JSONObject json2=new JSONObject(rt2.value);
                    success=json2.optBoolean("success");
                    if(!success){
                        throw new Exception("获取姓名失败，没能成功登录portal！");
                    }
                    String realName=json2.optString("userName");
                    Message message=new Message();
                    message.what=SHOW_NAME;
                    //将服务器返回的数据存放到Message中
                    message.obj=realName;
                    handler.sendMessage(message);
                }catch (Exception e){
                    Message message=new Message();
                    message.what=SHOW_ERROR;
                    //将服务器返回的数据存放到Message中
                    message.obj=e.getMessage();
                    handler.sendMessage(message);
                }
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
