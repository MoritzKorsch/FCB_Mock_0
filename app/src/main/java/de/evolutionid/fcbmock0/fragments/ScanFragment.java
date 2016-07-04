package de.evolutionid.fcbmock0.fragments;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import de.evolutionid.fcbmock0.MainActivity;
import de.evolutionid.fcbmock0.R;

public class ScanFragment extends Fragment{


    NfcAdapter nfcAdapter;
    Tag tag;

    //region GUI elements
    Spinner spnMode;
    RelativeLayout rlText;
    TextView txtTagContentText;
    LinearLayout llTextToTag;
    EditText edtWriteToTag;
    Button btnWriteText;
    //endregion

    public ScanFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scan, container, false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Initialize the NFC component
        nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());

        //region GUI element initialization
        //(order according to design)
        spnMode = (Spinner) getView().findViewById(R.id.spnMode);
        rlText = (RelativeLayout) getView().findViewById(R.id.rlText);
        txtTagContentText = (TextView) getView().findViewById(R.id.txtTagContentText);
        llTextToTag = (LinearLayout) getView().findViewById(R.id.llTextToTag);
        edtWriteToTag = (EditText) getView().findViewById(R.id.edtWriteToTag);
        btnWriteText = (Button) getView().findViewById(R.id.btnWriteText);

        //endregion

        //region Some more GUI functionality
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.spnMode, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMode.setAdapter(adapter);
        //endregion

        //region OnClicks
        /**
         btnWriteText.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        // write the message onto tag, if possible.
        writeNdefMessage(tag, createNdefMessage(" " + edtWriteToTag.getText() + " "));
        // Clear the input.
        //edtWriteToTag.setText("");
        }
        });
         */

        btnWriteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Write the url onto tag, if possible.
                writeNdefMessage(tag, createNdefMessage(edtWriteToTag.getText().toString()));
            }
        });

        spnMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Show and hide some GUI elements depending on what mode is chosen
                switch (spnMode.getSelectedItem().toString()) {
                    case "Text":
                        edtWriteToTag.setInputType(1);
                        edtWriteToTag.setText(""); //clear text
                        break;
                    case "Points":
                        edtWriteToTag.setInputType(2); //only numbers allowed
                        edtWriteToTag.setText(""); //clear text
                        break;
                    case "Website":
                        edtWriteToTag.setInputType(1);
                        edtWriteToTag.setText("http://www.");
                        break;
                    default:
                        txtTagContentText.setText("spnMode.onItemSelected case default");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //endregion
    }

    /**
     * Creates a NdefMessage of the given String.
     *
     * @param content the message that is being translated into a NDEF message
     * @return the NDEF message containing the text record with the string
     */
    private NdefMessage createNdefMessage(String content) {
        NdefRecord ndefRecord;
        if (spnMode.getSelectedItem().toString().equals("Website")) {
            ndefRecord = createUrlRecord(content);
        } else if (spnMode.getSelectedItem().toString().equals("Points")) {
            ndefRecord = createPointsRecord(content);
        } else {
            ndefRecord = createTextRecord(content);
        }
        return new NdefMessage(new NdefRecord[]{ndefRecord});
    }

    /**
     * Creates a record from a given text (for UTF-8).
     *
     * @param content the text to be encoded
     * @return text as record
     */
    private NdefRecord createTextRecord(String content) {
        try {
            //If anything else than UTF-8 is needed, you can change it here
            byte[] language = Locale.getDefault().getLanguage().getBytes("UTF-8");
            final byte[] text = content.getBytes("UTF-8");
            final int languageLength = language.length;
            final int textLength = text.length;
            final ByteArrayOutputStream payload = new ByteArrayOutputStream(1 + languageLength + textLength);

            //Write the record
            payload.write((byte) (languageLength & 0x1F));
            payload.write(language, 0, languageLength);
            payload.write(text, 0, textLength);

            return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload.toByteArray());

        } catch (Exception e) {
            Toast.makeText(this.getActivity(), "Error: createTextRecord()", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return null;
        //Using the code below instead of the code above (all of it, as of 2016-03-08), is
        //the better choice, but can only be used by API level 21 or above.
        //return NdefRecord.createTextRecord(Locale.getDefault().getLanguage(), content);
    }

    private NdefRecord createPointsRecord(String inputPoints) {
        return createTextRecord("Points: " + inputPoints.replace(" ", ""));
    }

    private NdefRecord createUrlRecord(String inputUrl) {
        String url;
        if (inputUrl != null) {
            if (!inputUrl.startsWith("http://")) {
                if (!inputUrl.startsWith("www.")) {
                    url = "http://www." + inputUrl;
                } else {
                    url = "http://" + inputUrl;
                }
            } else {
                url = inputUrl;
            }
            return NdefRecord.createUri(url);
        }
        return null;
    }


    public void processNfcIntent(Bundle savedInstanceState) {
        Parcelable[] parcelables = savedInstanceState.getParcelableArray("message");
        //if there are messages
        if (parcelables != null && parcelables.length > 0) {
            readTextFromMessage((NdefMessage) parcelables[0]);
        } else {
            //Feedback if there are no messages
            edtWriteToTag.setText(R.string.rNoTextOnTag);
        }
    }

    /**
     * Reads all the Records in a NdefMessage and prints them out to a TextView.
     *
     * @param ndefMessage the message to be read
     */
    private void readTextFromMessage(NdefMessage ndefMessage) {
        //Read out all the Records contained in the ndefMessage
        NdefRecord[] ndefRecords = ndefMessage.getRecords();

        //If there are records
        if (ndefRecords != null && ndefRecords.length > 0) {
            NdefRecord ndefRecord = ndefRecords[0];
            String tagContent = getTextFromNdefRecord(ndefRecord);

            //If it's a point tag
            if (tagContent.startsWith("Points: ")) {
                //Get just the score (without the string) and pass it to the Activity
                ((MainActivity) getActivity()).transferPoints(
                        Integer.parseInt(tagContent.replace("Points: ", "")));
            }

            txtTagContentText.setText(tagContent);
        } else {
            //Feedback if there are no records
            txtTagContentText.setText(R.string.rNoRecordOnTag);
        }
    }

    /**
     * Reads one NdefRecord and returns it.
     *
     * @param ndefRecord the record to be read
     * @return the text of a record as a String
     */
    private String getTextFromNdefRecord(NdefRecord ndefRecord) {
        byte[] payload = ndefRecord.getPayload();

        String textEncoding;
        if ((payload[0] & 128) == 0) {
            textEncoding = "UTF-8";
        } else {
            textEncoding = "UTF-16";
        }

        //There is a warning here. I have not tried any solutions, since the code works perfectly fine.
        int languageCodeLength = payload[0] & 0063;

        try {
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(getActivity(), "Error: getTextFromNdefRecord()", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        //Feedback, if everything else fails.
        return "Error reading Tag. Contact developer!";
    }

    private void writeNdefMessage(Tag tag, NdefMessage ndefMessage) {
        try {
            // Checks if the tag is still in contact with the device.
            if (tag == null) {
                throw new NullPointerException("Error: writeNdefMessage() - Tag object is null");
            }
            // Get the tag
            Ndef ndef = Ndef.get(tag);
            // if the Tag is not formatted yet
            if (ndef == null) {
                // format it and immediately write the desired message on it.
                formatTag(tag, ndefMessage);
            } else {
                ndef.connect();
                // If the tag is not writable (i.e. set to read-only)
                if (!ndef.isWritable()) {
                    // Give some Feedback and close the connection to the Tag
                    Toast.makeText(getActivity(), "Tag is not writable!", Toast.LENGTH_SHORT).show();
                    ndef.close();
                    return;
                }

                /* If you run into problems with size limitations, uncomment this!
                if(ndef.getMaxSize() - ndefMessage.getByteArrayLength() < 0) {
                    Toast.makeText(this, "Text to large for tag!", Toast.LENGTH_SHORT).show();
                    ndef.close();
                    return;
                } */

                ndef.writeNdefMessage(ndefMessage);
                ndef.close();
                Toast.makeText(getActivity(), "Tag written!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error: writeNdefMessage()", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * Formats a tag to NDEF format and writes a message to it.
     *
     * @param tag         the tag object
     * @param ndefMessage the message to put on the tag after formatting it
     */
    private void formatTag(Tag tag, NdefMessage ndefMessage) {
        try {
            NdefFormatable ndefFormatable = NdefFormatable.get(tag);

            //If the tag cannot be formatted, give Feedback and return
            if (ndefFormatable == null) {
                Toast.makeText(getActivity(), "Tag is not NDEF formatable!", Toast.LENGTH_SHORT).show();
                return;
            }

            //Connect, format and write to tag, disconnect, give feedback.
            ndefFormatable.connect();
            ndefFormatable.format(ndefMessage);
            ndefFormatable.close();
            Toast.makeText(getActivity(), "Tag formatted!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error: formatTag()", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

}