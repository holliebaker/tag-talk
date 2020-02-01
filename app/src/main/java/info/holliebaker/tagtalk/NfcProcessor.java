package info.holliebaker.tagtalk;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.speech.tts.TextToSpeech;

import java.io.UnsupportedEncodingException;

public class NfcProcessor {
    private static final String TEXT_PLAIN = "text/plain";

    public class InvalidMessageException extends Exception {
        public InvalidMessageException(String message) {
            super(message);
        }
    }

    public NdefMessage[] getMessages(Intent intent) throws InvalidMessageException {
        Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        if (rawMessages == null) {
            throw new InvalidMessageException(
                    "No messages found in EXTRA_NDEF_MESSAGES"
            );
        }

        NdefMessage[] messages = new NdefMessage[rawMessages.length];
        for (int i = 0; i < rawMessages.length; i++) {
            messages[i] = (NdefMessage) rawMessages[i];
        }

        return messages;
    }

    public String getMessagePlainText(NdefMessage message) throws InvalidMessageException, UnsupportedEncodingException {
        NdefRecord record = message.getRecords()[0];

        String mimeType = record.toMimeType();
        if (mimeType == null || !mimeType.equals(TEXT_PLAIN)) {
            throw new InvalidMessageException(
                    "Invalid mime type of " + (mimeType != null ? mimeType : "null") +
                            "expected " + TEXT_PLAIN
            );
        }

        byte[] payload = record.getPayload();
        if (payload.length == 0) {
            throw new InvalidMessageException(
                    "Record has empty payload"
            );
        }

        String encoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
        int languageCodeLength = payload[0] & 0063;

        return new String(
                payload,
                languageCodeLength + 1,
                payload.length - languageCodeLength - 1,
                encoding
        );
    }
}
