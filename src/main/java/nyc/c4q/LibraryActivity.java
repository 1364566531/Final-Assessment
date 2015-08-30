package nyc.c4q;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;


public class LibraryActivity extends Activity {

    public EditText inputParameter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        inputParameter = (EditText) findViewById(R.id.input_parameter);

        new AsyncTask<Void, Void, List<String>>(){
            @Override
            protected String doInBackground(Void[] params){
                List<String[]> insert = new ArrayList<>();

                try {
                    InputStreamReader is = new InputStreamReader(getAssets().open("New_York_City_Leading_Causes_of_Death.csv"));
                    Scanner scan = new Scanner(is);

                    if (scan.hasNextLine())
                        scan.nextLine();

                    while (scan.hasNextLine()) {
                        String[] line = scan.nextLine().split(",");
                        insert.add(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                updateData(insert);
                return loadData();
            }

            @Override
            protected void onPostExecute(List<String> o)
            {
                showData(o);
            }
        }.execute();


        createBooks(loadBooksJson());
    }

    public void checkOut(int memberId, int bookId) {
        // TODO This method is called when the member with the given ID checks
        //      out the book with the given ID. Update the system accordingly.
        //      The due date for the book is two weeks from today.
    }

    public boolean checkIn(int memberId, int bookId) {
        // TODO This method is called when the member with the given ID returns
        //      the book with the given ID. Update the system accordingly. If
        //      the member is returning the book on time, return true. If it's
        //      late, return false.

        return false;
    }

    public void button_getMember_onClick(View view) {
        String name = inputParameter.getText().toString();

        // TODO Display member information for the member with the given name.
    }

    public void button_getBook_onClick(View view) {
        String isbn = inputParameter.getText().toString();

        // TODO Display book information for the book with the given ISBN.
    }

    public void button_getCheckedOut_onClick(View view) {
        String name = inputParameter.getText().toString();

        // TODO Display a list of books that the member with the given name
        //      currently has checked out, ordered by due date, with the
        //      earliest due first.
    }

    public String loadBooksJson() {
        String json = null;
        try {
            InputStream is = this.getAssets().open("/Users/sufeizhao/Desktop/Final-Assessment/src/main/res/raw/books.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public void createBooks(String json) {




        try {
            JSONObject obj = new JSONObject(json);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
