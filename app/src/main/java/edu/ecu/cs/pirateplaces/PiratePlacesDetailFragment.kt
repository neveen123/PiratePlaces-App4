package edu.ecu.cs.pirateplaces

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.*
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.util.*

private const val ARG_PLACE_ID = "place_id"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_DATE = 0
private const val REQUEST_CONTACT = 1
private const val REQUEST_PHOTO = 2
private const val DATE_FORMAT = "EEE, MMM, dd"
private const val REQUEST_TIME = 1

class PiratePlacesDetailFragment:
    Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.Callbacks {

    private lateinit var place: PiratePlace
    private lateinit var placeNameField: EditText
    private lateinit var guestsField: TextView
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView
    private lateinit var shareButton: Button
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri

    private val piratePlacesDetailViewModel: PiratePlacesDetailViewModel by lazy {
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
        shareButton = view.findViewById(R.id.share_visit) as Button

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(place.lastVisited).apply {
                setTargetFragment(this@PiratePlacesDetailFragment, REQUEST_DATE)
                show(this@PiratePlacesDetailFragment.parentFragmentManager, DIALOG_DATE)
            }
        }
        shareButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getPiratePlaceReport())
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.share_report_subject)
                )
            }.also { intent ->
                val chooserIntent =
                    Intent.createChooser(intent, getString(R.string.share_visit))
                startActivity(chooserIntent)
            }
        }

        guestsField.apply {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }

            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(
                    pickContactIntent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
            if (resolvedActivity == null) {
                isEnabled = false
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
            Observer { piratePlace ->
                piratePlace?.let {
                    this.place = piratePlace
                    photoFile = piratePlacesDetailViewModel.getPhotoFile(place)
                    photoUri = FileProvider.getUriForFile(
                        requireActivity(), "edu.ecu.cs.pirateplaces.fileprovider",
                        photoFile
                    )
                    updateUI()
                }
            })

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

        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager
            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(
                    captureImage,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
            if (resolvedActivity == null) {
                isEnabled = false
            }
            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                val cameraActivities: List<ResolveInfo> =
                    packageManager.queryIntentActivities(
                        captureImage,
                        PackageManager.MATCH_DEFAULT_ONLY
                    )
                for (cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }
                startActivityForResult(captureImage, REQUEST_PHOTO)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        piratePlacesDetailViewModel.savePiratePlace(place)
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(
            photoUri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
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
        updatePhotoView()
    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            photoView.setImageBitmap(bitmap)
        } else {
            photoView.setImageDrawable(null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return
            requestCode == REQUEST_CONTACT && data != null -> {
                data.data?.let {
                    val contactUri: Uri = it
                    // Specify which fields you want your query to return values for
                    val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                    // Perform your query - the contactUri is like a "where" clause here
                    val cursor = requireActivity().contentResolver
                        .query(contactUri, queryFields, null, null, null)

                    cursor?.use {
                        // Verify cursor contains at least one result
                        if (it.count == 0) {
                            return
                        }
                        // Pull out the first column of the first row of data -
                        // that is your suspect's name
                        it.moveToFirst()
                        val contact = it.getString(0)
                        place.visitedWith = contact
                        piratePlacesDetailViewModel.savePiratePlace(place)
                        guestsField.text = contact
                    }
                }
            }
            requestCode == REQUEST_PHOTO -> {
                requireActivity().revokeUriPermission(
                    photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                updatePhotoView()
            }
        }
    }

    private fun getPiratePlaceReport(): String {

        val dateString = DateFormat.format(DATE_FORMAT, place.lastVisited).toString()
        val visitedTime = DateFormat.getTimeFormat(context).format(place.lastVisited)

        if (place.visitedWith.isBlank()) {
            return getString(R.string.share_visit_mssg2,place.name, place.visitedWith,dateString,visitedTime)
        }
        else{return getString(
            R.string.share_visit_mssg,
            place.name,
            place.visitedWith,
            dateString,
            visitedTime)
        }
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