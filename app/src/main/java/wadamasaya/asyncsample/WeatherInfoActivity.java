package wadamasaya.asyncsample;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeatherInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_info);

        ListView lvCityList = findViewById(R.id.lvCityList);
        List<Map<String,String>> cityList = new ArrayList<>();
        Map<String,String> city = new HashMap<>();
        city.put("name" , "大阪");
        city.put("id","270000");
        cityList.add(city);
        city = new HashMap<>();
        city.put("name", "神戸");
        city.put("id", "280010");
        cityList.add(city);

        String [] from = {"name"};
        int[] to = {android.R.id.text1};
        SimpleAdapter adapter = new SimpleAdapter(WeatherInfoActivity.this,cityList,android.R.layout.simple_expandable_list_item_1,from,to);
        lvCityList.setAdapter(adapter);
        lvCityList.setOnItemClickListener(new ListItemClickListener());
    }

    private class ListItemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view,int position,long id) {
            Map<String,String> item = (Map<String, String>) parent.getItemAtPosition(position);
            String cityName = item.get("name");
            String cityId = item.get("id");
            TextView tvCityName = findViewById(R.id.tvCityName);
            tvCityName.setText(cityName + "の天気:");

            TextView tvWeatherTelop = findViewById(R.id.tvWeatherTelop);
            TextView tvWeatherDesc = findViewById(R.id.tvWeatherDesc);
            WeatherInfoReceiver receiver = new WeatherInfoReceiver(tvWeatherTelop,tvWeatherDesc);
            receiver.execute(cityId);
        }
    }

    private class WeatherInfoReceiver extends AsyncTask<String,String,String> {
        private TextView _tvWeatherTelop;

        private TextView _tvWeatherDesc;

        public WeatherInfoReceiver(TextView tvWeatherTelop,TextView tvWeatherDesc){
            _tvWeatherTelop = tvWeatherTelop;
            _tvWeatherDesc = tvWeatherDesc;
        }

        @Override
        public String doInBackground(String... params){
            String id = params[0];
            String urlStr = "http://weather.livedoor.com/forecast/webservise/json/v1?city=" + id;
            String result = "";

            HttpURLConnection con = null;
            InputStream is = null;
            try {
                URL url = new URL(urlStr);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.connect();
                is = con.getInputStream();
                result = is2String(is);
            }
            catch (MalformedURLException ex){
            }
            catch (IOException ex){
            }
            finally {
                if (con != null){
                    con.disconnect();
                }
                if (is != null){
                    try {
                        is.close();
                    }
                    catch (Exception ex){
                    }
                }
            }

            return result;
        }

        @Override
        public void onPostExecute(String result){
            String telop = "";
            String desc = "";

            try {
                JSONObject rootJSON = new JSONObject(result);
                JSONObject descriptionJSON = rootJSON.getJSONObject("description");
                desc = descriptionJSON.getString("text");
                JSONArray forecasts = rootJSON.getJSONArray("forecasts");
                JSONObject forecastNow = forecasts.getJSONObject(0);
                telop = forecastNow.getString("telop");
            }
            catch (JSONException ex){
            }

            _tvWeatherTelop.setText(telop);
            _tvWeatherDesc.setText(desc);
        }
        private String is2String(InputStream is) throws IOException{
            BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
            StringBuffer sb = new StringBuffer();
            char[] b = new char[1024];
            int line;
            while (0 <= (line = reader.read(b))){
                sb.append(b,0,line);
            }
            return sb.toString();
        }
    }
}
