import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import com.github.loooris.zonetasker.R

class ReminderFragment : Fragment(R.layout.fragment_reminder) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Trigger Field Handling
        // Set up the auto complete text view
        val triggerList = resources.getStringArray(R.array.trigger_list)
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, triggerList)

        val autoCompleteTextView = view.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)
        autoCompleteTextView.setAdapter(adapter)
        autoCompleteTextView.threshold = 1
    }
}