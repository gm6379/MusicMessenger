package georgemcdonnell.com.musicmessenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private EditText messageTextField;
    private EditText phoneNumberTextField;
    private NotesView notesView;
    private ArrayList<Note> notes;
    private ArrayList<byte[]> buffers = new ArrayList<>();
    private int currentNote;
    private int currentBuffer;
    private NoteHelper noteHelper = new NoteHelper();

    private static final String SMS_RECEIVED =
            "android.provider.Telephony.SMS_RECEIVED";

    private final int SAMPLE_RATE = 8000;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageTextField = (EditText) findViewById(R.id.message_field);
        phoneNumberTextField = (EditText) findViewById(R.id.phone_number_field);

        Button sendButton = (Button) findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        Button addToStaveButton = (Button) findViewById(R.id.add_to_stave_button);
        addToStaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageTextField.getEditableText().toString();
                if (isValid(message)) {
                    addMessageToStave(message);
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.invalid_message), Toast.LENGTH_LONG).show();
                }
            }
        });

        Button playButton = (Button) findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageTextField.getEditableText().toString();
                if (isValid(message)) {
                    notes = noteHelper.parseNotes(MainActivity.this, message);
                    buffers = new ArrayList<>();
                    currentNote = 0;
                    currentBuffer = 0;
                    createBuffer(notes.get(currentBuffer));
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.invalid_message), Toast.LENGTH_LONG).show();
                }
            }
        });

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                Object[] pdus = (Object[]) bundle.get("pdus");
                for (Object pduObj:pdus) {
                    byte[] pdu = (byte[]) pduObj;
                    SmsMessage message = SmsMessage.createFromPdu(pdu);
                    processMessage(message);
                }
            }
        };

        IntentFilter filter = new IntentFilter(SMS_RECEIVED);
        registerReceiver(receiver, filter);

        notesView = (NotesView) findViewById(R.id.notes_view);
    }

    private void processMessage(SmsMessage message) {
        String messageText = message.getMessageBody();
        Scanner scan = new Scanner(messageText);
        if(scan.hasNext()) {
            if (isValid(messageText)) {
                addMessageToStave(messageText);
                if (notes != null) {
                    buffers = new ArrayList<>();
                    currentNote = 0;
                    currentBuffer = 0;
                    createBuffer(notes.get(currentBuffer));
                }
            }
        }
    }

    private void sendMessage() {
        SmsManager manager = SmsManager.getDefault();
        String message = messageTextField.getEditableText().toString();
        String phoneNumber = phoneNumberTextField.getEditableText().toString();

        if (phoneNumber != null && !phoneNumber.equals("") && message != null && !message.equals("")) {
            manager.sendTextMessage(phoneNumber, null, message, null, null);
        } else if (message == null || message.equals("")) {
            Toast.makeText(this, getString(R.string.message_empty), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, getString(R.string.phone_empty), Toast.LENGTH_LONG).show();
        }
    }

    private boolean isValid(CharSequence s) {
        return NoteHelper.pattern.matcher(s).matches();
    }

    private void addMessageToStave(String messageText) {
        notes = noteHelper.parseNotes(MainActivity.this, messageText);
        notesView.setNotes(notes);
        notesView.invalidate();
    }

    private byte[] fillBuffer(double freq, int duration) {
        int sampleSize = duration * SAMPLE_RATE;
        byte[] audioBuffer = new byte[2 * sampleSize];
        for (int i = 0; i < audioBuffer.length / 2; i++) {
            double val = Math.sin(2 * Math.PI * i / (SAMPLE_RATE / freq));
            short normVal = (short) ((val*32767));
            audioBuffer[2*i] = (byte) (normVal & 0x00ff);
            audioBuffer[2*i+1] = (byte) ((normVal & 0xff00) >>> 8);
        }
        return audioBuffer;
    }

    private void playBuffer(byte[] audioBuffer) {
        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, audioBuffer.length,
                AudioTrack.MODE_STATIC);

        audioTrack.setNotificationMarkerPosition(notes.get(currentNote).getDuration() * SAMPLE_RATE);
        audioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
            @Override
            public void onMarkerReached(AudioTrack track) {
                audioTrack.release();
                if (currentNote != buffers.size() - 1) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            currentNote++;
                            playBuffer(buffers.get(currentNote));
                        }
                    });
                }
            }

            @Override
            public void onPeriodicNotification(AudioTrack track) {}
        });

        audioTrack.write(audioBuffer, 0, audioBuffer.length);
        audioTrack.play();
    }

    protected void createBuffer(Note note) {
        final double finFreq = note.getFrequency();
        final int duration = note.getDuration();
        final Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                final byte [] buffer = fillBuffer(finFreq,  duration);
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        buffers.add(buffer);

                        currentBuffer++;

                        if (buffers.size() == notes.size()) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    playBuffer(buffers.get(currentNote));
                                }
                            });
                        } else {
                            createBuffer(notes.get(currentBuffer));
                        }
                    }
                });
            }
        });

        thread.start();
    }

}
