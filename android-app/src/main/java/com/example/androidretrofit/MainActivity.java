package com.example.androidretrofit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class MainActivity extends AppCompatActivity {

    private static final String API_URL = "https://api.github.com";
    
    // 数据模型
    public static class Contributor {
        public String login;
        public int contributions;
    }

    // Retrofit 接口
    interface GitHub {
        @GET("/repos/square/retrofit/contributors")
        Call<List<Contributor>> contributors();
    }

    private RecyclerView recyclerView;
    private TextView statusText;
    private ContributorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContributorAdapter();
        recyclerView.setAdapter(adapter);

        findViewById(R.id.buttonFetch).setOnClickListener(v -> fetchContributors());
    }

    private void fetchContributors() {
        statusText.setText("加载中...");
        statusText.setVisibility(android.view.View.VISIBLE);
        recyclerView.setVisibility(android.view.View.GONE);

        // 创建 Retrofit 实例
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // 创建 API 接口实例
        GitHub github = retrofit.create(GitHub.class);

        // 发起请求
        github.contributors().enqueue(new Callback<List<Contributor>>() {
            @Override
            public void onResponse(Call<List<Contributor>> call, Response<List<Contributor>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    statusText.setVisibility(android.view.View.GONE);
                    recyclerView.setVisibility(android.view.View.VISIBLE);
                    adapter.setContributors(response.body());
                } else {
                    statusText.setText("获取数据失败");
                    Toast.makeText(MainActivity.this, "错误: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Contributor>> call, Throwable t) {
                statusText.setText("网络错误");
                Toast.makeText(MainActivity.this, "错误: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // RecyclerView 适配器
    class ContributorAdapter extends RecyclerView.Adapter<ContributorAdapter.ViewHolder> {
        private List<Contributor> contributors = new ArrayList<>();

        void setContributors(List<Contributor> contributors) {
            this.contributors = contributors;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_contributor, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Contributor contributor = contributors.get(position);
            holder.login.setText(contributor.login);
            holder.contributions.setText(String.valueOf(contributor.contributions));
        }

        @Override
        public int getItemCount() {
            return contributors.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView login;
            TextView contributions;

            ViewHolder(View itemView) {
                super(itemView);
                login = itemView.findViewById(R.id.login);
                contributions = itemView.findViewById(R.id.contributions);
            }
        }
    }
}

