package info.holliebaker.tagtalk;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class AddActivity extends AppCompatActivity {
    TextView inputText;
    TextView labelWriteStatus;
    private NfcAdapter nfcAdapter;
    private NfcProcessor nfcProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        inputText = (TextView) findViewById(R.id.input_text);
        labelWriteStatus = (TextView) findViewById(R.id.label_write_status);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcProcessor = new NfcProcessor();
    }

    @Override
    protected void onPause() {
        nfcAdapter.disableForegroundDispatch(this);

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // liston for NFC tags
        setupForegroundDispatch();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (!intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED) &&
                !intent.getAction().equals(NfcAdapter.ACTION_TECH_DISCOVERED)) {
            Log.d(MainActivity.TAG, "Unhandled intent action: " + intent.getAction());

            return;
        }

        String textToWrite = inputText.getText().toString();

        if (textToWrite.isEmpty()) {
            labelWriteStatus.setText(R.string.label_write_status_error_no_text);

            return;
        }

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        try{
            nfcProcessor.writeMessagePlainText(tag, textToWrite);

            labelWriteStatus.setText(R.string.label_write_status_success);
        } catch (NfcProcessor.MessogeWriteException e) {
            labelWriteStatus.setText(R.string.label_write_status_error + " - " + e.getMessage());

            Log.d(MainActivity.TAG, e.getMessage());
        }

       ;
    }

    private void setupForegroundDispatch() {
        String[][] techList = new String[][]{
                new String[]{Ndef.class.getName()}
        };
        final Intent intent = new Intent(this, AddActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilters = new IntentFilter[1];
        intentFilters[0] = new IntentFilter();
        intentFilters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        intentFilters[0].addCategory(Intent.CATEGORY_DEFAULT);

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, techList);
    }
}
