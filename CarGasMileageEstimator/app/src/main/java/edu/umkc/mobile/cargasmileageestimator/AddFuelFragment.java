package edu.umkc.mobile.cargasmileageestimator;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import edu.umkc.mobile.cargasmileageestimator.model.MileageModel;
import edu.umkc.mobile.cargasmileageestimator.data.CarDetailsCollection;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddFuelFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddFuelFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddFuelFragment extends android.support.v4.app.Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public static  String restoredEmailId ="";
    public static String editedRestoredEmailId = "";


    TextView textFuelDetails;
    TextView textFuelAdded;
    EditText editTextFuelAdded;
    Button btnUpdateFuel;
    TextView textTotalFuel;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    MileageModel model = new MileageModel();

    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    static Double fuelInDB = 0.0;
    static Double totalFuelInDB = 0.0;
    static Double totalFuelCost = 0.0;


    String INSERT_API_URL = "http://192.168.1.8:8080/api/insertCarDetails/";
    String GET_API_URL = "http://192.168.1.8:8080/api/getCarDetails/";
    CarDetailsCollection carDetailsCollection;

    private OnFragmentInteractionListener mListener;

    public AddFuelFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddFuelFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddFuelFragment newInstance(String param1, String param2) {
        AddFuelFragment fragment = new AddFuelFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view =  inflater.inflate(R.layout.fragment_add_fuel, container, false);



        textFuelDetails = (TextView)view.findViewById(R.id.textFuelDetails);
        textFuelAdded = (TextView)view.findViewById(R.id.textFuelAdded);
        editTextFuelAdded = (EditText)view.findViewById(R.id.editTextFuelAdded);
        btnUpdateFuel = (Button)view.findViewById(R.id.btnUpdateFuel);
        textTotalFuel= (TextView)view.findViewById(R.id.textTotalFuel);

        SharedPreferences prefs = getActivity().getSharedPreferences(LoginActivity.MyPREFERENCES, 0);
        //restoredEmailId = prefs.getString("email", "");
        restoredEmailId = user.getEmail().toString();
        editedRestoredEmailId = restoredEmailId.replaceAll("\\.", "dot");


        new AddFuelFragment.HttpGetAsyncTask().execute(GET_API_URL+editedRestoredEmailId);

        btnUpdateFuel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Double enteredFuel = Double.parseDouble(editTextFuelAdded.getText().toString());
                Double totalFuel = fuelInDB + enteredFuel;
                //Update fuel and push it to db.
                carDetailsCollection = new CarDetailsCollection();
                carDetailsCollection.setEmailId(editedRestoredEmailId);
                carDetailsCollection.setFuel(Double.toString((double)Math.round((totalFuel)*100.0)/100.0));
                Double totalGas = totalFuelInDB + enteredFuel;
                carDetailsCollection.setTotalGas(Double.toString((double)Math.round((totalGas)*100.0)/100.0));
                Double totalGasCost = totalFuelCost + (totalFuel * 2.56);
                carDetailsCollection.setTotalGasCost( Double.toString((double)Math.round((totalGasCost)*100.0)/100.0));
                new AddFuelFragment.HttpAsyncTask().execute(INSERT_API_URL);

                textTotalFuel.setText("fuel remaining: " + String.valueOf(totalFuel));
            }
        });
        return view;
    }

    public void FetchCurrentFuel()
    {
        //fetch current fuel from db and set it to textTotalFuel
    }

    public void UpdateFuel()
    {
        //Update fuel and push it to db and update textTotalFuel
    }
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            //Toast.makeText(context, "Add Fuel Fragment Attached", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return model.POST_CarDetails(urls[0],carDetailsCollection);

        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getContext(), "Data Sent!", Toast.LENGTH_LONG).show();
        }
    }

    private class HttpGetAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return model.GET(urls[0]);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getContext(), "Received!", Toast.LENGTH_LONG).show();
            try {
                if (result != null && !"".equalsIgnoreCase(result)) {
                    JSONObject json = new JSONObject(result);
                    textTotalFuel.setText("fuel remaining: " +json.getString("fuel").toString());
                    fuelInDB = Double.parseDouble(json.getString("fuel").toString());
                    totalFuelInDB = Double.parseDouble(json.getString("totalGas").toString());
                    totalFuelCost = Double.parseDouble(json.getString("totalGasCost").toString());
                }

            } catch (Exception e) {

            }

        }
    }

}
