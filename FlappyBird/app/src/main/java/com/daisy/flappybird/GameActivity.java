package com.daisy.flappybird;

import android.content.DialogInterface;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;


public class GameActivity extends AppCompatActivity {

    public native synchronized void LED_CTRL(int num);
    public native void LED_ALL_ON();
    public native void LED_ALL_OFF();
    public native void JUMPSOUND();
    public native void DEADSOUND();
    static{System.loadLibrary("GAMEACT");}


    private GameView gameView;
    private TextView textViewScore;
    private boolean isGameOver;
    private boolean isSetNewTimerThreadEnabled;
    //
    public int jump_cnt;
    //public void plus(){jump_cnt += 1;}//동기화시켜줘서 오류값이 안뜨게해줌
    //public void minus(){jump_cnt -= 1;}
    public synchronized void plus_minus(int num){//0일때 더하기 나머지 마이너스
        if (num==0){jump_cnt++;}
        else{jump_cnt--;}
    }

    //
    private Thread setNewTimerThread;
    private Thread increaseJump;

    private AlertDialog.Builder alertDialog;

    private MediaPlayer mediaPlayer;


    private Timer timer;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case UPDATE: {
                    if (gameView.isAlive()) {
                        isGameOver = false;
                        gameView.update();
                    } else {
                        if (isGameOver) {
                            break;
                        } else {
                            isGameOver = true;
                        }
                            // Cancel the timer
                            timer.cancel();
                            timer.purge();
                            //led 원상복귀

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    LED_ALL_OFF();
                                }
                            }).start();
                            //dead sound
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    DEADSOUND();
                                }
                            }).start();





                            increaseJump.interrupt();

                        alertDialog = new AlertDialog.Builder(GameActivity.this);
                        alertDialog.setTitle("GAME OVER");
                        alertDialog.setMessage("Score: " + String.valueOf(gameView.getScore()) +
                                "\n" + "Would you like to RESTART?");
                        alertDialog.setCancelable(false);
                        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                GameActivity.this.restartGame();
                            }
                        });
                        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                GameActivity.this.onBackPressed();
                            }
                        });
                        alertDialog.show();
                    }

                    break;
                }

                case RESET_SCORE: {
                    textViewScore.setText("0");

                    break;
                }

                default: {
                    break;
                }
            }
        }
    };

    // The what values of the messages
    private static final int UPDATE = 0x00;
    private static final int RESET_SCORE = 0x01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {//시작점 여기서 file open
        super.onCreate(savedInstanceState);

        // 스테이터스바 안보이게
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //

        setContentView(R.layout.activity_game);
        initViews();

        // timer
        isSetNewTimerThreadEnabled = true;
        setNewTimerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Sleep for 3 seconds for the Surface to initialize
                    Thread.sleep(3000);
                } catch (Exception exception) {
                    exception.printStackTrace();
                } finally {
                    if (isSetNewTimerThreadEnabled) {
                        setNewTimer();
                    }
                }
            }
        });
        setNewTimerThread.setDaemon(true);
        setNewTimerThread.start();
        //******************************************************
        //jump_cnt full and start
        LED_ALL_ON();
        jump_cnt = 8;
        increaseJump = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    while(true) {
                        if (jump_cnt < 8) {
                            //plus();
                            plus_minus(0);
                            LED_CTRL(jump_cnt);
                        }
                        Thread.sleep(1050);
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                } finally {

                }
            }
        });
        increaseJump.setDaemon(true);
        increaseJump.start();
        //********************************************


            // Jump listener
            gameView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if(jump_cnt > 0) {//if count is left
                                //minus();
                                plus_minus(1);
                                gameView.jump();
                                //jump and decrease**************

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        LED_CTRL(jump_cnt);
                                    }
                                }).start();

                                ///jump sound*********************
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        JUMPSOUND();
                                    }
                                }).start();

                            }
                                    //점프하는곳


                            break;

                        case MotionEvent.ACTION_UP:


                            break;

                        default:
                            break;
                    }

                    return true;
                }
            });

    }

    private void initViews() {
        gameView = findViewById(R.id.game_view);
        textViewScore = findViewById(R.id.text_view_score);
    }

    /**
     * Sets the Timer to update the UI of the GameView.
     */
    private void setNewTimer() {
        if (!isSetNewTimerThreadEnabled) {
            return;
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                // Send the message to the handler to update the UI of the GameView
                GameActivity.this.handler.sendEmptyMessage(UPDATE);//

                // For garbage collection
                System.gc();
            }

        }, 0, 17);
    }

    protected void onDestroy() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }

        isSetNewTimerThreadEnabled = false;
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        isSetNewTimerThreadEnabled = false;

        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    /**
     * Updates the displayed score.
     *
     * @param score The new score.
     */
    public void updateScore(int score) {
        textViewScore.setText(String.valueOf(score));
    }//점수표기?


    private void restartGame() {
        // Reset all the data of the over game in the GameView

        gameView.resetData();

        // Refresh the TextView for displaying the score
        new Thread(new Runnable() {

            @Override
            public void run() {
                handler.sendEmptyMessage(RESET_SCORE);
            }

        }).start();


            isSetNewTimerThreadEnabled = true;
            setNewTimerThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        // Sleep for 3 seconds
                        Thread.sleep(3000);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    } finally {
                        if (isSetNewTimerThreadEnabled) {
                            setNewTimer();
                        }
                    }
                }

            });
            setNewTimerThread.start();
            //restart, jump_cnt full and start************************
        LED_ALL_ON();
        jump_cnt = 8;
        increaseJump = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    while(true) {
                        if (jump_cnt < 8) {
                            //plus();
                            plus_minus(0);
                            LED_CTRL(jump_cnt);
                        }
                        Thread.sleep(1050);
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                } finally {

                }
            }
        });
        increaseJump.setDaemon(true);
        increaseJump.start();
        //******************************************

    }

    @Override
    public void onBackPressed() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }

        isSetNewTimerThreadEnabled = false;

        super.onBackPressed();
    }

}
