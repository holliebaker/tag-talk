package info.holliebaker.tagtalk;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

public class NfcDetectedActivity extends AppCompatActivity {
    public static final String TAG = "TagTalk:debug";
    public static final String BUNDLE_KEY_TAG_TEXT = "tag-text";
    public static final String BUNDLE_KEY_NEED_TO_SPEAK = "need-to-speak-text";

    TextView labelText;
    private NfcProcessor nfcProcessor = new NfcProcessor();
    private TextToSpeechHelper textToSpeech;
    private View layoutContainer;

    private String tagText;
    private boolean needToSpeakText = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        restoreSavedState(savedInstanceState);

        labelText = (TextView) findViewById(R.id.label_text);
        layoutContainer = (View) findViewById(R.id.layout_container);
        layoutContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tagText != null) {
                    speak(tagText);
                }
            }
        });

        textToSpeech = new TextToSpeechHelper(this, 1);

        // this activity should have only been started by an intent activated by NFC,
        // and we want to filter for only NDEF tags
        Intent intent = this.getIntent();
        if (intent == null || !intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            return;
        }

        if (tagText == null) {
            processNfcLabel(intent);
        }

        labelText.setText(tagText);

        // ensures that label is spoken automatically only once
        if (needToSpeakText) {
            speak(tagText);

            needToSpeakText = false;
        }
    }

    private void speak(String text) {
        try {
            textToSpeech.speak(text);
        } catch (TextToSpeechHelper.QueueFullException e) {
            Log.d(TAG, "Unable to speak text because the queue is full.");
        }
    }

    private void processNfcLabel(Intent intent) {
        try {
            NdefMessage[] messages = nfcProcessor.getMessages(intent);

            if (messages.length != 1) {
                throw new Exception(
                        "Expected 1 message, got " + messages.length
                );
            }

            tagText = nfcProcessor.getMessagePlainText(messages[0]);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());

            tagText = getString(R.string.unexpected_error);
        }
    }
    @Override
    public void onSaveInstanceState(Bundle state) {
        // prevents reading and processing the tag data multiple times,
        // for example when the screen is rotated
        if (tagText != null) {
            state.putString(BUNDLE_KEY_TAG_TEXT, tagText);
        }

        state.putBoolean(BUNDLE_KEY_NEED_TO_SPEAK, needToSpeakText);


        super.onSaveInstanceState(state);
    }

    private void restoreSavedState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }

        // restore tag text if the tag has been read before
        // onRestoreInstanceState is not used because it is called AFTER onCreate, and that is too late
        tagText = savedInstanceState.getString(BUNDLE_KEY_TAG_TEXT);
        needToSpeakText = savedInstanceState.getBoolean(BUNDLE_KEY_NEED_TO_SPEAK);
    }
}
