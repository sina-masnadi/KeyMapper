package io.github.sds100.keymapper.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.sds100.keymapper.Action
import io.github.sds100.keymapper.ActionType
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.serial.CommandService
import kotlinx.android.synthetic.main.action_type_edit_text.*
import kotlinx.android.synthetic.main.action_type_edit_text.buttonDone
import kotlinx.android.synthetic.main.action_type_edit_text.editText
import kotlinx.android.synthetic.main.action_type_serial.*
import kotlinx.android.synthetic.main.recyclerview_fragment.textViewCaption

/**
 * Created by sina on 13/03/2020.
 */

/**
 * A Fragment which allows a user to send serial command over USB
 */
class SerialActionTypeFragment : ActionTypeFragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.action_type_serial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textViewCaption.setText(R.string.caption_action_serial)

        buttonDone.setOnClickListener {
            Log.d("service", "S1")


            Log.d("service", "S2")

            val action = Action(ActionType.SERIAL, editText.text.toString())
            chooseSelectedAction(action)
        }
        buttonTestCommand.setOnClickListener {
            val intent = Intent(activity, CommandService::class.java)
            intent.putExtra(CommandService.PARAM_COMMAND, editText.text.toString())
            activity?.startService(intent)
        }
    }
}