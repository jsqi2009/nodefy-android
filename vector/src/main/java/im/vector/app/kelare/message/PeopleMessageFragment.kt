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

package im.vector.app.kelare.message

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.databinding.FragmentHistoryBinding
import im.vector.app.databinding.FragmentPeopleMessageBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class PeopleMessageFragment : VectorBaseFragment<FragmentPeopleMessageBinding>() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
            FragmentPeopleMessageBinding.inflate(inflater, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                PeopleMessageFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
