import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.loooris.zonetasker.R
import com.google.android.material.textfield.TextInputEditText

class ReminderFragment : Fragment(R.layout.fragment_reminder) {

    companion object {
        var message = ""
        var selectedTrigger = ""
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val messageField = view.findViewById<TextInputEditText>(R.id.messageField)
        val autoCompleteTextView = view.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)

        // Trigger Field Handling
        // Set up the auto complete text view
        val triggerList = resources.getStringArray(R.array.trigger_list)
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, triggerList)
        autoCompleteTextView.setAdapter(adapter)
        autoCompleteTextView.threshold = 1

        // Set an item click listener on the AutoCompleteTextView
        autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            selectedTrigger = parent.getItemAtPosition(position).toString()
            Toast.makeText(context, "Selected Value: $selectedTrigger", Toast.LENGTH_SHORT).show()
        }

        // Update the message variable when messageField value changes
        messageField.setOnEditorActionListener { _, _, _ ->
            message = messageField.text.toString()
            hideKeyboard()
            true
        }
    }

    // Add this function to hide the keyboard when the user validates the message.
    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocus = requireActivity().currentFocus
        if (currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }
}
