package nyc.c4q;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

public class PaceCalculatorActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pace_calculator);

        // alessandro: why are you not using the support library?
        // you should have used getSupportFragmentManager here
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        PaceCalculatorFragment fragment = new PaceCalculatorFragment();
        transaction.add(R.id.activity_pace_calculator, fragment);
        transaction.commit();
    }

}
