package edu.ecu.cs.pirateplaces

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import java.util.*

private const val ARG_PLACE_ID = "place_id"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_DATE = 0
private const val REQUEST_TIME = 1

class PiratePlacesDetailFragment:
    Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.Callbacks {

    private lateinit var place: PiratePlace
    private lateinit var placeNameField : EditText
    private lateinit var guestsField: TextView
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView

    private val piratePlacesDetailViewModel : PiratePlacesDetailViewModel by lazy {
        ViewModelProvider(this).get(PiratePlacesDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        place = PiratePlace()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pirate_places_detail, container, false)

        placeNameField = view.findViewById(R.id.place_name) as EditText
        guestsField = view.findViewById(R.id.visited_with) as TextView
        dateButton = view.findViewById(R.id.check_in_date) as Button
        timeButton = view.findViewById(R.id.check_in_time) as Button
        photoButton = view.findViewById(R.id.place_camera) as ImageButton
        photoView = view.findViewById(R.id.place_photo) as ImageView

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(place.lastVisited).apply {
                setTargetFragment(this@PiratePlacesDetailFragment, REQUEST_DATE)
                show(this@PiratePlacesDetailFragment.parentFragmentManager, DIALOG_DATE)
            }
        }

        timeButton.setOnClickListener {
            TimePickerFragment.newInstance(place.lastVisited).apply {
                setTargetFragment(this@PiratePlacesDetailFragment, REQUEST_TIME)
                show(this@PiratePlacesDetailFragment.parentFragmentManager, DIALOG_TIME)
            }
        }

        photoButton.setOnClickListener {
            Toast.makeText(context, getString(R.string.camera_message), Toast.LENGTH_SHORT).show()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        piratePlacesDetailViewModel.piratePlaceLiveData.observe(
            viewLifecycleOwner,
            Observer {  piratePlace ->
                piratePlace?.let {
                    this.place = piratePlace
                    updateUI()
                }
            }
        )

        val placeId = arguments?.getSerializable(ARG_PLACE_ID) as UUID
        piratePlacesDetailViewModel.loadPiratePlace(placeId)
    }

    override fun onStart() {
        super.onStart()

        val placeNameWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Do nothing
            }

            override fun afterTextChanged(p0: Editable?) {
                // Do nothing
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                place.name = p0.toString()
            }
        }

        placeNameField.addTextChangedListener(placeNameWatcher)
    }

    override fun onStop() {
        super.onStop()
        piratePlacesDetailViewModel.savePiratePlace(place)
    }

    override fun onDateSelected(date: Date) {
        place.lastVisited = date
        updateUI()
    }

    override fun onTimeSelected(date: Date) {
        place.lastVisited = date
        updateUI()
    }

    private fun updateUI() {
        val visitedDate = DateFormat.getMediumDateFormat(context).format(place.lastVisited)
        val visitedTime = DateFormat.getTimeFormat(context).format(place.lastVisited)

        placeNameField.setText(place.name)
        guestsField.setHint(R.string.visited_with_hint)
        guestsField.text = place.visitedWith
        dateButton.text = visitedDate
        timeButton.text = visitedTime
    }

    companion object {
        fun newInstance(id: UUID) : PiratePlacesDetailFragment {
            val args = Bundle().apply {
                putSerializable(ARG_PLACE_ID, id)
            }
            return PiratePlacesDetailFragment().apply {
                arguments = args
            }
        }
    }

}