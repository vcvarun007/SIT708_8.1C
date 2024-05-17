package com.example.llama2chatbot;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    EditText messageInput;
    ImageView sendButton;
    String username;
    List<String[]> chatMessages;
    RecyclerView chatRecyclerView;
    private RequestQueue requestQueueInstance;
    JSONArray chatHistory;
    MessageAdapter adapterInstance;

    @Override
    protected void onCreate(Bundle instanceState) {
        super.onCreate(instanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        messageInput = findViewById(R.id.chat_input);
        messageInput.setInputType(InputType.TYPE_NULL);
        sendButton = findViewById(R.id.button_send);
        chatRecyclerView = findViewById(R.id.chat_rv);
        username = getIntent().getStringExtra("name");
        chatHistory = new JSONArray();
        chatMessages = new ArrayList<>();
        requestQueueInstance = Volley.newRequestQueue(this);
        adapterInstance = new MessageAdapter(chatMessages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(adapterInstance);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userMessage = messageInput.getText().toString();
                String[] newMessage = new String[]{userMessage, "user"};
                adapterInstance.addItem(newMessage);
                messageInput.setText("");
                JSONObject messagePayload = createMessagePayload(userMessage);
                makeRequest(ChatActivity.this, "http://10.0.2.2:5000/chat", messagePayload, userMessage);
            }
        });
    }

    private JSONObject createMessagePayload(String userMessage) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("userMessage", userMessage);
            payload.put("chatHistory", chatHistory);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return payload;
    }

    private void makeRequest(Context context, String url, JSONObject payload, String userMessage) {
        System.out.println(payload);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, payload,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String aiReply = response.getString("message");
                            System.out.println("aiReply : " + aiReply);
                            JSONObject chatRecord = new JSONObject();
                            chatRecord.put("User", userMessage);
                            chatRecord.put("Llama", aiReply);
                            chatHistory.put(chatRecord);
                            String[] newMessage = new String[]{aiReply, "ai"};
                            adapterInstance.addItem(newMessage);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        getRequestQueueInstance(context).add(request);
    }

    private RequestQueue getRequestQueueInstance(Context context) {
        if (requestQueueInstance == null) {
            requestQueueInstance = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueueInstance;
    }

    private class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
        List<String[]> messages;

        public MessageAdapter(List<String[]> messages) {
            this.messages = messages;
        }

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            String[] msg = messages.get(position);
            holder.bind(msg);
        }

        public void addItem(String[] newItem) {
            messages.add(newItem);
            notifyItemInserted(messages.size() - 1);
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        public class MessageViewHolder extends RecyclerView.ViewHolder {
            TextView userInitialTextView, messageTextView;
            ImageView aiImageView;
            CardView messageCard;

            public MessageViewHolder(@NonNull View itemView) {
                super(itemView);
                aiImageView = itemView.findViewById(R.id.img_ai);
                userInitialTextView = itemView.findViewById(R.id.username_init_bubble);
                messageTextView = itemView.findViewById(R.id.tv_message);
                messageCard = itemView.findViewById(R.id.cv_text);
            }

            public void bind(String[] msg) {
                userInitialTextView.setText((username.charAt(0) + "").toUpperCase());
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) messageCard.getLayoutParams();
                if(msg[1].equals("ai")) {
                    aiImageView.setVisibility(View.VISIBLE);
                    userInitialTextView.setVisibility(View.INVISIBLE);
                } else {
                    aiImageView.setVisibility(View.INVISIBLE);
                    userInitialTextView.setVisibility(View.VISIBLE);
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                    messageCard.setLayoutParams(params);
                }
                messageTextView.setText(msg[0]);
            }
        }
    }
}
