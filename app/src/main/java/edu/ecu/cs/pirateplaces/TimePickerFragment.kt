package edu.ecu.cs.pirateplaces

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

private const val ARG_DATE = "date"

class TimePickerFragment: DialogFragment() {

    interface Callbacks {
        fun onTimeSelected(date: Date)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val date = arguments?.getSerializable(ARG_DATE) as Date
        calendar.time = date

        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)
        val initialHours = calendar.get(Calendar.HOUR_OF_DAY)
        val initialMinutes = calendar.get(Calendar.MINUTE)

        val timeListener = TimePickerDialog.OnTimeSetListener {
                _: TimePicker, hours: Int, minutes: Int ->

            val resultDate : Date = GregorianCalendar(initialYear, initialMonth, initialDay, hours, minutes).time
            targetFragment?.let {fragment ->
                (fragment as Callbacks).onTimeSelected(resultDate)
            }
        }

        return TimePickerDialog(
            requireContext(),
            timeListener,
            initialHours,
            initialMinutes,
            true
        )
    }

    companion object {
        fun newInstance(date: Date) : TimePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date)
            }

            return TimePickerFragment().apply {
                arguments = args
            }
        }
    }
}