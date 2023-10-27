package com.example.arjun.su_bca;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.example.arjun.su_bca.Utils.MessageChatGPT;
import com.example.arjun.su_bca.Utils.MessageChatGPTAdapter;
import com.example.arjun.su_bca.games.GamesActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatGPT extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private volatile String apiKey = "";
    private boolean isFirst = true;
    private RecyclerView recyclerView;
    private TextView welcomeText;
    private EditText messageEditText;
    private ImageButton sendButton;
    private Toolbar toolbar;
    private List<MessageChatGPT> messageList;
    private MessageChatGPTAdapter messageChatGPTAdapter;
    public final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_gpt);

        setupUIViews();
        setupGPTKey();
        initToolbar();
        sendButtonEval();

        // Setup Recycler View
        messageChatGPTAdapter = new MessageChatGPTAdapter(messageList);
        recyclerView.setAdapter(messageChatGPTAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void setupGPTKey () {

        if (isFirst) {
            apiKey = sharedPreferences.getString("ChatGPTAPI", "");
            isFirst = false;
        }
    }

    private void setupUIViews() {

        recyclerView = findViewById(R.id.rv_chat_gpt);
        welcomeText = findViewById(R.id.welcome_text);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_button);
        toolbar = findViewById(R.id.ToolbarCharGPT);
        messageList = new ArrayList<>();
        utility.setupGPTKey(ChatGPT.this);
        sharedPreferences = getSharedPreferences("App", Context.MODE_PRIVATE);

    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chat GPT");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void sendButtonEval() {
        sendButton.setOnClickListener(v -> {
            String question = messageEditText.getText().toString().trim();
            addToChat(question, MessageChatGPT.SENT_BY_ME);
            messageEditText.setText("");
            callApiMethod(question);
            welcomeText.setVisibility(View.GONE);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addToChat(String message, String sentBy) {
        runOnUiThread(() -> {
            messageList.add(new MessageChatGPT(message, sentBy));
            messageChatGPTAdapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(messageChatGPTAdapter.getItemCount());
        });
    }

    private void addResponse(String response) {
        messageList.remove(messageList.size()-1);
        addToChat(response, MessageChatGPT.SENT_BY_BOT);
    }

    private void callApiMethod(String question) {
        String copyQuestion = question.toLowerCase();
        if ((copyQuestion.contains("who")) && ((copyQuestion.contains("god arjun")) || (copyQuestion.contains("arjun gangwar")))) {
            messageList.add(new MessageChatGPT("Typing...", MessageChatGPT.SENT_BY_BOT));
            String newMessage = "Arjun is an Android Developer.\nA Student of Computer Science.\nHe's an Hobbyist Photographer and loves to Code or you can also say a computer hobbyist.\nI am also his beautiful creation.\nFor more details you can visit to: dev-arjun.cf or follow him on instagram: ohi_arj.";
            addResponse(newMessage);
        } else if ( ((copyQuestion.contains("instagram handle")) || (copyQuestion.contains("instagram account"))) && ((copyQuestion.contains("arjun gangwar")) || (copyQuestion.contains("god arjun")))) {
            messageList.add(new MessageChatGPT("Typing...", MessageChatGPT.SENT_BY_BOT));
            String newMessage = "Instagram username of Arjun is:\nohi_arj";
            addResponse(newMessage);
        } else if (copyQuestion.contains(".love")) {
            messageList.add(new MessageChatGPT("Typing...", MessageChatGPT.SENT_BY_BOT));
            addResponse(utility.accessLoveCalculator(copyQuestion));
        } else if (copyQuestion.contains(".game")) {
            Intent intent = new Intent(ChatGPT.this, GamesActivity.class);
            startActivity(intent);
        } else if (copyQuestion.contains(".refresh")) {
            isFirst = true;
            sharedPreferences.edit().putBoolean("isChatGPTAPI", false).apply();
            utility.setupGPTKey(ChatGPT.this);
            isFirst = true;
            setupGPTKey();
            messageList.add(new MessageChatGPT("Chat GPT is back to life, hope you find" +
                    " the answers to your questions and have a great time here.", MessageChatGPT.SENT_BY_BOT));
        } else if (copyQuestion.equals(".help")) {
            messageList.add(new MessageChatGPT("Following are the hidden commands:\n\n" +
                    "1. Help\n\nsyntax: .help\n\n(lists downs all the available commands)\n\n" +
                    "2. Love\n\nsyntax: .love <name1> <name2>\n\n(calculates the love between two names through an formula)\n\n" +
                    "3. Game\n\nsyntax: .game\n\n(shows hidden games, which you can play with your mates)\n\n" +
                    "3. Refresh\n\nsyntax: .refresh\n\n(if you gets an error like insufficient funds in your account.\nthen this command might resolve that error.)\n\n\n" +
                    "Note: enter the given commands correctly to invoke them. all commands start with a dot before them.\n", MessageChatGPT.SENT_BY_BOT));
        }
        else {
            if(isNetworkAvailable()) {
                //okhttp setup

                messageList.add(new MessageChatGPT("Typing...", MessageChatGPT.SENT_BY_BOT));
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("model", "text-davinci-003");
                    jsonBody.put("prompt", question);
                    jsonBody.put("max_tokens", 4000);
                    jsonBody.put("temperature", 0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
                Request request = new Request.Builder()
                        .url("https://api.openai.com/v1/completions")
                        .header("Authorization", "Bearer " + apiKey)
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        addResponse("Failed to load response due to " + e.getMessage());
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) {
                        if (response.isSuccessful()) {
                            JSONObject jsonObject;
                            try {
                                assert response.body() != null;
                                jsonObject = new JSONObject(response.body().string());
                                JSONArray jsonArray = jsonObject.getJSONArray("choices");
                                String result = jsonArray.getJSONObject(0).getString("text");
                                addResponse(result.trim());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                assert response.body() != null;
                                String errorMessage = response.body().string().toLowerCase();
                                if (errorMessage.contains("api key") || errorMessage.contains("bearer your_key")) {
                                    messageList.remove(messageList.size()-1);
                                    isFirst = true;
                                    setupGPTKey();
                                    callApiMethod(question);
                                } else {
                                    addResponse("Failed to load response due to " + errorMessage);
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                });
            } else {
                addToChat("Failed to load message due to no internet connection", MessageChatGPT.SENT_BY_BOT);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}