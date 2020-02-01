package info.holliebaker.tagtalk;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

public class NfcDetectedActivity extends AppCompatActivity {
    public static final String TAG = "TagTalk:debug";
    TextView labelText;
    private NfcProcessor nfcProcessor = new NfcProcessor();
    private TextToSpeechHelper textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        labelText = (TextView) findViewById(R.id.label_text);
        textToSpeech = new TextToSpeechHelper(this, 1);

        // this activity should have only been started by an intent activated by NFC,
        // and we want to filter for only NDEF tags
        Intent intent = this.getIntent();
        if (intent == null || !intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            return;
        }

        try {
            NdefMessage[] messages = nfcProcessor.getMessages(intent);

            if (messages.length != 1) {
                throw new Exception(
                        "Expected 1 message, got " + messages.length
                );
            }

            String messageText = nfcProcessor.getMessagePlainText(messages[0]);

            labelText.setText(messageText);
            textToSpeech.speak(messageText);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());

            labelText.setText(R.string.unexpected_error);
        }
    }
}
