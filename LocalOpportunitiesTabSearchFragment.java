package com.example.ikoala.ui.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ikoala.R;
import com.example.ikoala.adapters.SearchResultsAdapter;
import com.example.ikoala.logger.Log;
import com.example.ikoala.ui.ParentActivity;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class LocalOpportunitiesTabSearchFragment extends Fragment
{

    private SearchResultsAdapter adapter;
    private RecyclerView searchResultsView;
    private TextView resultTextView;
    private Location mLastKnownLocation;
    private TextView postcodeLabel;
    private static final String KEY_LOCATION = "location";
    private Button searchButton;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    //Postcode.io location
    private final String POSTCODEIO_ENDPOINT = "https://api.postcodes.io/postcodes/";
    private final String GOOGLE_CUSTOM_SEARCH_ENDPOINT = "https://www.googleapis.com/customsearch/v1?";
    private final String RESULTS_LIMIT = "5";

    //Custom search variables
    Integer responseCode = null;
    String responseMessage = "";
    private Context mContext;
    private String location = "Bath";
    private String query = "Default query";
    private String postcode = "BA";
    private static final String API_KEY = "AIzaSyBBSnAZOqmavii0imK03cioNooWPZT1KZk";
    private static final String SEARCH_ENGINE_ID = "011117269733478716815:zk3g56kifsk";
    private static final int REQUEST_LOCATION = 123;
    private ArrayList<CustomSearchFormattedOutput> searchResultsList = new ArrayList<>();
    public View view;

    private String genericLink_location1 = "https://www.versusarthritis.org/in-your-area/";
    private String genericLink_location1_label = "Versus Arthritis:";
    private TextView genericLink1;
    private TextView genericLink1Label;
    private TextView disclaimerDescriptionView;
    private String disclaimerDescription = "iKOALA does not manage the content  of the external pages that are referenced below. You are encouraged to determine the reliability of the content of these pages before acting on it.";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle passedBundle = getArguments();
        if(passedBundle != null){
            if(passedBundle.containsKey("customSearchQuery")){
                query = passedBundle.getString("customSearchQuery");
                postcode = passedBundle.getString("postcode");
            }
        }
        view = inflater.inflate(R.layout.fragment_tab_localopportunities_search,container,false);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        resultTextView = view.findViewById(R.id.results_text_view);
        searchResultsView =  view.findViewById(R.id.custom_search_recyclerView);

        genericLink1 = view.findViewById(R.id.generic_link_1);
        genericLink1Label = view.findViewById(R.id.generic_link_1_label);
        disclaimerDescriptionView = view.findViewById(R.id.disclaimer_description);
        disclaimerDescriptionView.setText(disclaimerDescription);
        genericLink1.setText(genericLink_location1);
        genericLink1Label.setText(genericLink_location1_label);
        genericLink1.setOnClickListener(item -> {

            LinkAlert.show(genericLink1.getText().toString(), mContext);
        });

        searchButton = view.findViewById(R.id.custom_search_button);
        postcodeLabel = view.findViewById(R.id.edit_postcode);
        postcodeLabel.setText(postcode);

        searchResultsView.setNestedScrollingEnabled(false);
        searchResultsView.setHasFixedSize(true);
        searchResultsView.setLayoutManager(new LinearLayoutManager(getActivity()));

        searchButton.setOnClickListener(v-> {
            startSearch();
        });

        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void startSearch(){
        if(postcode.isEmpty()){
            Toast.makeText(getContext(), "Postcode invalid", Toast.LENGTH_SHORT).show();
        }else
        {
            String code = getLocationFromPostCode(postcode);
            if (!code.equals("error"))
            {
                search(code);
                searchButton.setText("Done");
            }
            else
            {
                Toast.makeText(getContext(), "Invalid postcode", Toast.LENGTH_SHORT).show();
                searchButton.setText("Search");
            }
        }
    }

    /**
     * Obtaining locality from postcode
      */
    private String getLocationFromPostCode(String code){

        String searchStringNoSpaces = code.replace(" ", "");
        String urlString = POSTCODEIO_ENDPOINT + searchStringNoSpaces;

        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            Log.e("localOpportunities", "ERROR converting String to URL " + e.toString());
        }

        LocalOpportunitiesTabSearchFragment.CustomSearchAsyncTask searchTask = new LocalOpportunitiesTabSearchFragment.CustomSearchAsyncTask();
        String result = "";

        //Converting result object to json
        try {
            searchTask.execute(url);
            result = searchTask.get();

            JSONObject jsonObject = new JSONObject(result).getJSONObject("result");
            PostCodeFormattedOutput.ResultsOutput filteredOutput;
            Gson g = new Gson();
            filteredOutput = g.fromJson(jsonObject.toString(), PostCodeFormattedOutput.ResultsOutput.class);
            filteredOutput.parish = filteredOutput.parish.replaceAll(", unparished area", "");
            return filteredOutput.parish;

        }catch(ExecutionException | InterruptedException | JSONException e ) {
            android.util.Log.e("localOpportunities", "Http Response ERROR " + e.toString());
            return "error";
        }
    }

    /**
     * Custom search code below
     */
    //Code modified from github project: https://github.com/fanysoft/Android_Google_Custom_SearchDemo
    private void search(String location){

        String[] firstPartOfParish = location.split(" ");
        String txt = "Opportunities in " + firstPartOfParish[0];
        firstPartOfParish[0] =  firstPartOfParish[0] + "+UK";
        query = query.replaceAll("location", firstPartOfParish[0]);
        String searchStringNoSpaces = query.replace(" ", "+");
        resultTextView.setText(txt);
        String urlString = GOOGLE_CUSTOM_SEARCH_ENDPOINT + "q=" + searchStringNoSpaces +"&key=" + API_KEY + "&cx=" + SEARCH_ENGINE_ID + "&alt=json" + "&num=" + RESULTS_LIMIT;

        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            Log.e("localOpportunities", "ERROR converting String to URL " + e.toString());
        }
        Log.d("localOpportunities", "Url = "+  urlString);

        //Code modified from github project: https://github.com/fanysoft/Android_Google_Custom_SearchDemo
        LocalOpportunitiesTabSearchFragment.CustomSearchAsyncTask searchTask = new LocalOpportunitiesTabSearchFragment.CustomSearchAsyncTask();
        String result = "";

        //Converting result object to json
        try {
            searchTask.execute(url);
            result = searchTask.get();
            JsonFactory factory = new JsonFactory();
            ObjectMapper mapper = new ObjectMapper(factory);
            JsonNode rootNode = mapper.readTree(result);

            Iterator<Map.Entry<String, JsonNode>> fieldsIterator = rootNode.fields();
            while (fieldsIterator.hasNext()) {
                Map.Entry<String, JsonNode> field = fieldsIterator.next();

                if (field.getKey().equals("items")) {
                    //Save array to string
                    String resultsArrayString = field.getValue().toString();
                    LocalOpportunitiesTabSearchFragment.CustomSearchFormattedOutput filteredOutput;

                    JSONArray arr = new JSONArray(resultsArrayString);
                    List<String> list = new ArrayList<String>();
                    for (int i = 0; i < arr.length(); i++) {
                        list.add(arr.getJSONObject(i).toString());
                        Gson g = new Gson();
                        filteredOutput = g.fromJson(arr.getJSONObject(i).toString(), CustomSearchFormattedOutput.class);
                        searchResultsList.add(filteredOutput);
                    }
                }
            }
        }catch(IOException | ExecutionException | JSONException | InterruptedException e ) {
            android.util.Log.e("localOpportunities", "Http Response ERROR " + e.toString());
        }

        //Once object is converted, show results
        resultTextView.setMovementMethod(new ScrollingMovementMethod()); //scrollable text field
        showSearchResults();

    }

    private class CustomSearchAsyncTask extends AsyncTask<URL, Integer, String> {

        @Override
        protected String doInBackground(URL... urls) {

            URL url = urls[0];
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
                responseCode = conn.getResponseCode();
                responseMessage = conn.getResponseMessage();
            } catch (IOException e) {
                android.util.Log.e("localOpportunities", "Http connection ERROR " + e.toString());
            }

            try {
                if(responseCode != null && responseCode == 200) {
                    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;

                    while ((line = rd.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    rd.close();
                    conn.disconnect();
                    return sb.toString();

                }else{
                    String errorMsg = "Http ERROR response " + responseMessage + "\n" + "Are you online ? " + "\n";
                    android.util.Log.e("localOpportunities", errorMsg);
                    return  errorMsg;
                }
            } catch (IOException e) {
                android.util.Log.e("localOpportunities", "Http Response ERROR " + e.toString());
            }
            return null;
        }

    }

    private void showSearchResults() {
        adapter = new SearchResultsAdapter(searchResultsList);
        searchResultsView.setAdapter(adapter);

        //Disabling search button if results have been retrieved
        searchButton.setEnabled(false);

        adapter.setOnItemClickListener((item, position) -> {
            LinkAlert.show(item.getLink(), mContext);
        });
    }

    //Class used to format output from custom search
    public static class CustomSearchFormattedOutput {
        private String title;
        private String link;

        public String getTitle() {
            return title;
        }
        public String getLink() {
            return link;
        }

    }

    //Class used to format output from postcode.io api
    public static class PostCodeFormattedOutput {

        private PostCodeFormattedOutput.ResultsOutput result;

        static class ResultsOutput {
            String parish;
        }
    }
}
