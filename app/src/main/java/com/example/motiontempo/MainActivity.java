package com.example.motiontempo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity{
    private Button btnTime;
    private Button btnPlay;
    private Button btnShank;
    private Button btnSound;
    private Button btnLight;
    private Button btnAdd;
    private Button btnMinus;
    private Button btnGap;
    private Button btnSelect;
    private Button btnLong;
    private Button btnShort;
    private Button btnJump;
    private Button btnGym;
    private TextView textBpm;
    private Timer timer;
    private Timer timerPlay;
    private Timer timerGap;
    private ImageView img;
    private LinearLayout lay;
    private int flag = 0;
    private int bpm = 120;
    private int max = 300;
    private int min = 10;
    private boolean playFlag;
    private boolean shankFlag;
    private int lightFlag;  //循环显示
    private boolean lightBtnFlag = true;  //闪烁按钮按下
    private boolean soundFlag = true;
    private boolean btnSoundFlag = true;  //判断声音按钮是否开启
    private boolean gapFlag;
    private boolean btnSelectFlag;
    private Vibrator vibrator;
    private SoundPool sp;
    private int id;
    private int second;  //倒计时秒数
    private int secondCon = 10;  //输入的倒计时常量
    private EditText edt;

    //倒计时线程
    private Handler handler = new Handler(){
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(@NonNull Message msg) {
            int count = msg.arg1;
            if (count == 0 ){
                edt.setText(count+"");
                playRecall();
                btnTime.setBackgroundResource(R.drawable.btn_restore);
                flag = 2;
                timer.cancel();
            }else{
                edt.setText(count+"");
            }
        }
    };

    //闪烁线程
    private Handler handlerLight = new Handler(){
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(@NonNull Message msg) {
            int f = msg.arg1;
            if (f == 1){
                img.setImageResource(R.drawable.back_red);
            }else {
                img.setImageResource(R.drawable.back_light);
            }
        }

    };

    //还原线程
    private Handler handlerReturn = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            int flag = msg.arg1;
            if (flag == 1){
                btnPlay.setBackgroundResource(R.drawable.play);
                img.setImageResource(R.drawable.back_light);
            }
        }
    };

    @SuppressLint({"ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        textBpm = findViewById(R.id.textView);
        btnTime = findViewById(R.id.button);
        btnPlay = findViewById(R.id.button3);
        btnShank = findViewById(R.id.button2);
        btnSound = findViewById(R.id.button7);
        btnLight = findViewById(R.id.button6);
        btnAdd = findViewById(R.id.button5);
        btnMinus = findViewById(R.id.button4);
        btnGap = findViewById(R.id.button8);
        btnSelect = findViewById(R.id.button13);
        btnLong = findViewById(R.id.button9);
        btnShort = findViewById(R.id.button10);
        btnJump = findViewById(R.id.button11);
        btnGym = findViewById(R.id.button12);
        img = findViewById(R.id.imageView);
        lay = findViewById(R.id.layoutImg);
        edt = findViewById(R.id.editTextTime);

        rota();
        countdown();
        play();
        Sound();
        initShank();
        initLight();
        initSound();
        monitor();
        addMinus();
        gap();
        setBtnSelect();
        setBtnLong();
        setBtnShort();
        setBtnJump();
        setBtnGym();
        vibrator = (Vibrator)this.getSystemService(VIBRATOR_SERVICE);

    }

    //开始按钮方法
    protected void play(){
        btnPlay.setOnClickListener(view -> {
            if (playFlag){
                playRecall();
            }else{
                btnPlay.setBackgroundResource(R.drawable.play_stop);
                playFlag = true;
                //循环按节奏显示
                timerPlay =new Timer();
                long delay =60000 / bpm;
                timerPlay.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        //循环震动
                        if (shankFlag){
                            vibrator.vibrate(100);
                        }
                        //循环声音
                        if (soundFlag){
                            playSound();
                        }
                    }
                },delay/2,delay);
                timerPlay.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Message message = new Message();
                        message.arg1 = lightFlag;
                        //循环闪烁
                        if (lightBtnFlag){
                            if (lightFlag == 1){
                                handlerLight.sendMessage(message);
                                lightFlag = 0;
                            }else{
                                handlerLight.sendMessage(message);
                                lightFlag = 1;
                            }
                        }
                    }
                },0,delay/2);
                //声音间隔播放
                int time = (int) (Math.random() * 3000);
                timerGap = new Timer();
                //停止时间
                timerGap.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (gapFlag && btnSoundFlag) {
                            soundFlag = !soundFlag;
                        }
                    }
                },0,time);

            }
        });
    }

    //开始按钮还原
    private void playRecall(){
        Message message = new Message();
        message.arg1 = 1;
        handlerReturn.sendMessage(message);
        vibrator.cancel();
        timerPlay.cancel();
        timerGap.cancel();
        playFlag = false;
        if (btnSoundFlag){
            soundFlag = true;
        }else{
            soundFlag = false;
        }
        lightFlag = 0;

    }

    //震动按钮方法
    protected void initShank(){
        btnShank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shankFlag){
                    btnShank.setBackgroundResource(R.drawable.shank_false);
                    shankFlag = false;
                }else{
                    btnShank.setBackgroundResource(R.drawable.shank_true);
                    shankFlag = true;
                }
            }
        });
    }

    //闪烁按钮方法
    protected void initLight(){
        btnLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lightBtnFlag){
                    btnLight.setBackgroundResource(R.drawable.light_false);
                    lightBtnFlag = false;
                }else{
                    btnLight.setBackgroundResource(R.drawable.light);
                    lightBtnFlag = true;
                }
            }
        });
    }

    //声音按钮方法
    protected void initSound(){
        btnSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnSoundFlag){
                    btnSound.setBackgroundResource(R.drawable.sound_false);
                    soundFlag = false;
                    btnSoundFlag = false;
                }else{
                    soundFlag = true;
                    btnSoundFlag = true;
                    btnSound.setBackgroundResource(R.drawable.sound);
                }
            }
        });
    }

    //旋转方法
    @SuppressLint("ClickableViewAccessibility")
    protected void rota(){
        //旋转效果
        lay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    //如果手指按下或者移动，那么图片也随之相应地旋转
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        if (!playFlag){
                            //设置现在手指触摸点的坐标
                            int degree = rotation(motionEvent.getX(),motionEvent.getY());
                            adjust(degree);
                            img.setRotation(degree);
                            break;
                        }
                    //如果手指抬起，则停止响应触摸事件
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return true;
            }
        });
    }
    //旋转方法
    private int rotation(double curX, double curY){
        int cX =img.getWidth()/2;  //中心点
        int cY = img.getHeight()/2;
        double r =Math.atan2(curX - cX, cY - curY);
        int degree = (int) Math.toDegrees(r);
        return degree;
    }
    //旋钮调节bpm
    private void adjust(int degree){
        if (min <= bpm && bpm <= max){
            int change = (int) (degree - img.getRotation());
            bpm += change;
            if (bpm > max){
                bpm = max;
            }else if (bpm < min){
                bpm = min;
            }
            textBpm.setText(""+bpm);
        }
    }

    //倒计时方法
    protected void countdown(){
        btnTime.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                second = secondCon;
                if (flag == 0){
                    flag = 1;
                    btnTime.setBackgroundResource(R.drawable.btn_time_stop);
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void run() {
                                Message message = new Message();
                                message.arg1 = second;
                            if (second >= 0) {
                                second--;
                                handler.sendMessage(message);
                            }else{
                                flag = 2;
                                timer.cancel();
                            }
                        }
                    },0,1000);
                }else if(flag == 1){
                    timer.cancel();
                    flag = 2;
                    btnTime.setBackgroundResource(R.drawable.btn_restore);
                }else if (flag == 2){
                    flag = 0;
                    btnTime.setBackgroundResource(R.drawable.btn_time);
                    edt.setText(secondCon + "");
                }
            }
        });
    }

    //声音
    private void Sound(){
        sp = new SoundPool.Builder().build();
        id =sp.load(this,R.raw.ding,0);
    }
    private void playSound(){
        sp.play(id,1f,1f,0,0,1);
    }

    //加减按钮方法
    private void addMinus(){
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!playFlag){
                    bpm++;
                    textBpm.setText(bpm+"");
                }
            }
        });
        btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!playFlag){
                    bpm--;
                    textBpm.setText(bpm+"");
                }
            }
        });
    }

    //监听edt
    private void monitor(){
        edt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edt.selectAll();
            }
        });

        edt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_DONE){
                    String text = edt.getText().toString();
                    if (TextUtils.isEmpty(text)){
                        secondCon = 0;
                        edt.setText("0");
                    }else{
                        secondCon = Integer.parseInt(text);
                    }
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edt.getWindowToken(), 0);
                    edt.clearFocus();
                    return true;
                }
                return false;
            }
        });
    }

    //间隔按钮
    protected void gap(){
        btnGap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (gapFlag){
                    gapFlag = false;
                    btnGap.setBackgroundResource(R.drawable.gap_false);
                    if (btnSoundFlag){
                        soundFlag = true;
                    }else{
                        soundFlag = false;
                    }
                }else{
                    gapFlag = true;
                    btnGap.setBackgroundResource(R.drawable.gap_true);
                }
            }
        });
    }

    //选择按钮
    protected void setBtnSelect(){
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnSelectFlag){
                    btnSelectFlag = false;
                    btnSelect.setBackgroundResource(R.drawable.list_false);
                    btnLong.setVisibility(View.GONE);
                    btnShort.setVisibility(View.GONE);
                    btnJump.setVisibility(View.GONE);
                    btnGym.setVisibility(View.GONE);
                    btnShank.setVisibility(View.GONE);
                    btnLight.setVisibility(View.GONE);
                    btnSound.setVisibility(View.GONE);
                    btnGap.setVisibility(View.GONE);
                }else{
                    btnSelectFlag = true;
                    btnSelect.setBackgroundResource(R.drawable.list_true);
                    btnLong.setVisibility(View.VISIBLE);
                    btnShort.setVisibility(View.VISIBLE);
                    btnJump.setVisibility(View.VISIBLE);
                    btnGym.setVisibility(View.VISIBLE);
                    btnShank.setVisibility(View.VISIBLE);
                    btnLight.setVisibility(View.VISIBLE);
                    btnSound.setVisibility(View.VISIBLE);
                    btnGap.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    //长跑按钮
    private void setBtnLong(){
        btnLong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!playFlag) {
                    bpm = 90;
                    textBpm.setText(bpm + "");
                }
            }
        });
    }
    //短跑按钮
    private void setBtnShort(){
        btnShort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!playFlag){
                    bpm = 130;
                    textBpm.setText(bpm+"");
                }
            }
        });
    }
    //连跳按钮
    private void setBtnJump(){
        btnJump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!playFlag) {
                    bpm = 80;
                    textBpm.setText(bpm + "");
                }
            }
        });
    }
    private void setBtnGym(){
        btnGym.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!playFlag) {
                    bpm = 60;
                    textBpm.setText(bpm + "");
                }
            }
        });
    }

    public void onInfo(View view) {
        if (!playFlag){
            Intent intent = new Intent(this, Info.class);
            startActivity(intent);
        }
    }
}