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

package im.vector.app.kelare.dialer.call

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityDialerCallBinding
import im.vector.app.databinding.ActivityDialerContactDetailBinding

@AndroidEntryPoint
class DialerCallActivity : VectorBaseActivity<ActivityDialerCallBinding>() {

    override fun getBinding() = ActivityDialerCallBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


}
