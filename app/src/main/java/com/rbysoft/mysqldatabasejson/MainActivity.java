package com.rbysoft.mysqldatabasejson;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.rbysoft.mysqldatabasejson.Dbhelper.Dbhelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog =new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("Please Wait.....");

        DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration configuration =new  ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(displayImageOptions)
                .build();
        ImageLoader.getInstance().init(configuration);

        listView = findViewById(R.id.mySQL_listView);

        new JSONTask().execute("https://indomitable71.000webhostapp.com/connect/connect.php");
    }

    private class JSONTask extends AsyncTask<String, String,List<Dbhelper>>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected List<Dbhelper> doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader((new InputStreamReader(stream)));
                StringBuilder builder = new StringBuilder();

                String line;

                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                String finalJSON = builder.toString();
                JSONObject parent_object = new JSONObject(finalJSON);
                JSONArray parent_array = parent_object.getJSONArray("server_responce");

                List<Dbhelper> dbhelperss = new ArrayList<>();
                Gson gson = new Gson();
                for (int i = 0; i < parent_array.length(); i++) {
                    JSONObject finalObject = parent_array.getJSONObject(i);
                    Dbhelper dbhelper1 = gson.fromJson(finalObject.toString(), Dbhelper.class);
                    dbhelperss.add(dbhelper1);
                }
                return dbhelperss;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                }catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
        }

        @Override
        protected void onPostExecute(List<Dbhelper> dbhelperesult) {
            super.onPostExecute(dbhelperesult);
            progressDialog.dismiss();
            DbhelperAdapter dbhelperAdapter = new DbhelperAdapter(getApplicationContext(),R.layout.mysqllview,dbhelperesult);
            listView.setAdapter(dbhelperAdapter);
        }
    }

    private class DbhelperAdapter extends ArrayAdapter {
        private List<Dbhelper>dbhelpers;
        private int resource;
        private LayoutInflater layoutInflater;

        public DbhelperAdapter(@NonNull Context context, int resource,  List<Dbhelper> objects) {
            super(context, resource, objects);
            dbhelpers=objects;
            this.resource=resource;
            layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }
        class ViewHolder {
            private ImageView person_img;
            private TextView txt_name;
            private  TextView txt_phone;
            private  TextView txt_address;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView==null){
                viewHolder = new ViewHolder();
                convertView = layoutInflater.inflate(resource,null);
                viewHolder.person_img = convertView.findViewById(R.id.imv);
                viewHolder.txt_name = convertView.findViewById(R.id.name);
                viewHolder.txt_phone = convertView.findViewById(R.id.phone);
                viewHolder.txt_address = convertView.findViewById(R.id.address);

                convertView.setTag(viewHolder);
            }
            else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final ProgressBar progressBar = convertView.findViewById(R.id.progressBar2);
            ImageLoader.getInstance().displayImage(dbhelpers.get(position).getImage(), viewHolder.person_img, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    progressBar.setVisibility(View.GONE);
                }
            });
            viewHolder.txt_name.setText(dbhelpers.get(position).getName());
            viewHolder.txt_phone.setText(dbhelpers.get(position).getPhone());
            viewHolder.txt_address.setText(dbhelpers.get(position).getAddress());
            return convertView;
        }
    }
}
