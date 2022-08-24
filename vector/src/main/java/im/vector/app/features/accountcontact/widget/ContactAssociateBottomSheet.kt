/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.app.features.accountcontact.widget

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.mvrx.args
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.core.platform.VectorBaseBottomSheetDialogFragment
import im.vector.app.databinding.BottomSheetContactAssociateBinding
import im.vector.app.databinding.BottomSheetSpaceInviteChooserBinding
import im.vector.app.features.spaces.SpaceBottomSheetSettingsArgs
import im.vector.app.features.spaces.SpaceSettingsMenuBottomSheet
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContactAssociateBottomSheetArgs(
        val type: String
) : Parcelable

@AndroidEntryPoint
class ContactAssociateBottomSheet : VectorBaseBottomSheetDialogFragment<BottomSheetContactAssociateBinding>() {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): BottomSheetContactAssociateBinding {
        return BottomSheetContactAssociateBinding.inflate(inflater, container, false)
    }

    interface InteractionListener {
        fun onRefreshRelations()
    }

    var interactionListener: InteractionListener? = null

    private val associateArgs: ContactAssociateBottomSheetArgs by args()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        fun newInstance(type: String, interactionListener: InteractionListener): ContactAssociateBottomSheet {
            return ContactAssociateBottomSheet().apply {
                this.interactionListener = interactionListener
                setArguments(ContactAssociateBottomSheetArgs(type))
            }
        }
    }
}
