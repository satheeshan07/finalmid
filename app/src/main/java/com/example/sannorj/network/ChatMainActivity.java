package com.example.sannorj.network;

import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ibm.mobilefirstplatform.clientsdk.android.analytics.api.*;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.*;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.*;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneHelper;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.developer_cloud.conversation.v1.Conversation;
import com.ibm.watson.developer_cloud.conversation.v1.model.InputData;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageOptions;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;

import org.json.JSONObject;

import java.util.ArrayList;

public class ChatMainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter mAdapter;
    private ArrayList messageArrayList;
    private EditText inputMessage;
    private ImageButton btnSend;
    private ImageButton btnRecord;

    private com.ibm.watson.developer_cloud.conversation.v1.model.Context context = null;
    StreamPlayer streamPlayer;
    private boolean initialRequest;
    private boolean permissionToRecordAccepted = false;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String TAG = "ChatMainActivity";
    private static final int RECORD_REQUEST_CODE = 101;
    private boolean listening = false;
    private SpeechToText speechService;
    private TextToSpeech textToSpeech;
    private MicrophoneInputStream capture;
    private Context mContext;
    private String workspace_id;
    private String conversation_username;
    private String conversation_password;
    private String STT_username;
    private String STT_password;
    private String TTS_username;
    private String TTS_password;
    private String analytics_APIKEY;

    private MicrophoneHelper microphoneHelper;
    private Logger myLogger;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatactivity_main);

        mContext = getApplicationContext();
        conversation_username = mContext.getString(R.string.conversation_username);
        conversation_password = mContext.getString(R.string.conversation_password);
        workspace_id = mContext.getString(R.string.workspace_id);
        //  STT_username = mContext.getString(R.string.STT_username);
        // STT_password = mContext.getString(R.string.STT_password);
        //  TTS_username = mContext.getString(R.string.TTS_username);
        //  TTS_password = mContext.getString(R.string.TTS_password);
        //  analytics_APIKEY = mContext.getString(R.string.mobileanalytics_apikey);


        //mobile Analytics
        BMSClient.getInstance().initialize(getApplicationContext(), BMSClient.REGION_SYDNEY);

        Analytics.init(getApplication(), "ChatBot", analytics_APIKEY, false,false, Analytics.DeviceEvent.ALL);
        //Analytics.send();
        myLogger = Logger.getLogger("myLogger");
        // Send recorded usage
        Analytics.send(new ResponseListener() {
            @Override
            public void onSuccess(Response response) {
                // Handle Analytics send success here.
            }

            @Override
            public void onFailure(Response response, Throwable throwable, JSONObject jsonObject) {
                // Handle Analytics send failure here.
            }
        });

        // Send logs to the Mobile Analytics Service
        Logger.send(new ResponseListener() {
            @Override
            public void onSuccess(Response response) {
                // Handle Logger send success here.
            }

            @Override
            public void onFailure(Response response, Throwable throwable, JSONObject jsonObject) {
                // Handle Logger send failure here.
            }
        });

        inputMessage = (EditText) findViewById(R.id.message);
        btnSend = (ImageButton) findViewById(R.id.btn_send);
        btnRecord= (ImageButton) findViewById(R.id.btn_record);
        String customFont = "Montserrat-Regular.ttf";
        Typeface typeface = Typeface.createFromAsset(getAssets(), customFont);
        inputMessage.setTypeface(typeface);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        messageArrayList = new ArrayList<>();
        mAdapter = new ChatAdapter(messageArrayList);
        microphoneHelper = new MicrophoneHelper(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        this.inputMessage.setText("");
        this.initialRequest = true;
        sendMessage();






////
//        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new ClickListener() {
//            @Override
//            public void onClick(View view, final int position) {
//                Thread thread = new Thread(new Runnable() {
//                    public void run() {
//                        Message audioMessage;
//                        try {
//
//                            audioMessage =(Message) messageArrayList.get(position);
//                            streamPlayer = new StreamPlayer();
//                            if(audioMessage != null && !audioMessage.getMessage().isEmpty())
//                                //Change the Voice format and choose from the available choices
//                                streamPlayer.playStream(textToSpeech.synthesize(audioMessage.getMessage(), Voice.EN_LISA).execute());
//                            else
//                                streamPlayer.playStream(textToSpeech.synthesize("No Text Specified", Voice.EN_LISA).execute());
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//                thread.start();
//            }
//
//            @Override
//            public void onLongClick(View view, int position) {
//              //  recordMessage();
//
//            }
//        }));



        btnSend.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(checkInternetConnection()) {
                    sendMessage();
                }
            }
        });


    };



    // sending a message to WatsonConversation
    private void sendMessage() {

        final String inputmessage = this.inputMessage.getText().toString().trim();
        if(!this.initialRequest) {
            Message inputMessage = new Message();
            inputMessage.setMessage(inputmessage);
            inputMessage.setId("1");
            messageArrayList.add(inputMessage);
            myLogger.info("Sending a message to Watson Conversation Service");

        }
        else
        {
            Message inputMessage = new Message();
            inputMessage.setMessage(inputmessage);
            inputMessage.setId("100");
            this.initialRequest = false;
            //  Toast.makeText(getApplicationContext(),"Tap on the message for Voice",Toast.LENGTH_LONG).show();

        }

        this.inputMessage.setText("");
        mAdapter.notifyDataSetChanged();

        Thread thread = new Thread(new Runnable(){
            public void run() {
                try {

                   // Conversation service = new Conversation(Conversation.VERSION_DATE_2018_07_10);
                    Conversation service = new Conversation("2018-07-10");
                    service.setUsernameAndPassword(conversation_username, conversation_password);
                    InputData input = new InputData.Builder(inputmessage).build();
                    MessageOptions options = new MessageOptions.Builder(workspace_id).input(input).context(context).build();
                    MessageResponse response = service.message(options).execute();

                    //Passing Context of last conversation
                    if(response.getContext() !=null)
                    {
                        //context.clear();
                        context = response.getContext();

                    }
                    final Message outMessage=new Message();
                    if(response!=null)
                    {
                        if(response.getOutput()!=null && response.getOutput().containsKey("text"))
                        {

                            ArrayList responseList = (ArrayList) response.getOutput().get("text");
                            if(null !=responseList && responseList.size()>0){
                                outMessage.setMessage((String)responseList.get(0));
                                outMessage.setId("2");
                            }
                            messageArrayList.add(outMessage);
                            Thread thread = new Thread(new Runnable() {
                                public void run() {
                                    Message audioMessage;

                                }
                            });
                            thread.start();
                        }

                        runOnUiThread(new Runnable() {
                            public void run() {
                                mAdapter.notifyDataSetChanged();
                                if (mAdapter.getItemCount() > 1) {
                                    recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount()-1);

                                }

                            }
                        });


                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

    }

    /**
     * Check Internet Connection
     * @return
     */
    private boolean checkInternetConnection() {
        // get Connectivity Manager object to check connection
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        // Check for network connections
        if (isConnected){
            return true;
        }
        else {
            Toast.makeText(this, " No Internet Connection available ", Toast.LENGTH_LONG).show();
            return false;
        }

    }



    private void showError(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                Toast.makeText(ChatMainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }

}
