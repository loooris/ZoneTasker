import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.github.loooris.zonetasker.R
import com.google.android.material.textfield.TextInputEditText

class MessageFragment : Fragment(R.layout.fragment_message) {

    private lateinit var contactsButton: Button
    private lateinit var phoneNumberField: TextInputEditText

    companion object {
        var selectedTrigger = ""
        var message = ""
        var phoneNumber = ""
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Set up click listener for contacts button
        val contactsButton = view.findViewById<Button>(R.id.contactsButton)
        contactsButton.setOnClickListener {
            // Create an intent to pick a phone number from contacts
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            // Launch the activity and handle the result with the activity result API
            pickPhoneNumber.launch(intent)
        }

        // Trigger Field Handling
        // Set up the auto complete text view
        val triggerList = resources.getStringArray(R.array.trigger_list)
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, triggerList)
        val autoCompleteTextView = view.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)
        autoCompleteTextView.setAdapter(adapter)
        autoCompleteTextView.threshold = 1

        // Set an item click listener on the AutoCompleteTextView
        autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            selectedTrigger = parent.getItemAtPosition(position).toString()
            Toast.makeText(context, "Selected Value: $selectedTrigger", Toast.LENGTH_SHORT).show()
        }

        // phoneNumber Field Handling
        val phoneNumberField = view.findViewById<TextInputEditText>(R.id.phoneNumberField)
        phoneNumberField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This method is called before the text is changed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // This method is called when the text is being changed
            }

            override fun afterTextChanged(s: Editable?) {
                // This method is called after the text has been changed
                phoneNumber = s.toString()
            }
        })



        // Message Field Handling
        // Set up the message text field
        val messageField = view.findViewById<TextInputEditText>(R.id.messageField)
        messageField.setOnEditorActionListener { _, actionId, _ ->
            message = messageField.text.toString()
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                messageField.clearFocus() // remove focus from the text field
                val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0) // hide the keyboard
                true // consume the event
            } else {
                false // do not consume the event
            }
        }
    }

    private val pickPhoneNumber = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            // Get the selected phone number from the contacts app
            val contactUri = result.data?.data ?: return@registerForActivityResult
            val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val cursor = requireContext().contentResolver.query(contactUri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val phoneNumber = it.getString(0)
                    phoneNumberField.setText(phoneNumber)
                }
            }
        }
    }

}
