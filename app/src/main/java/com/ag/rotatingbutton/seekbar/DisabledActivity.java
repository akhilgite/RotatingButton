/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013 Triggertrap Ltd
 * Author Neil Davies
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.ag.rotatingbutton.seekbar;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import com.ag.rotatingbutton.R;


/**
 * Sample to show seekarc in readonly state and setting progress/arc colors programmatically
 */
public class DisabledActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_disabled);

		SeekArc seekArcComplete = (SeekArc) findViewById(R.id.seekArcComplete);
		SeekArc seekArcWarning = (SeekArc) findViewById(R.id.seekArcWarning);

		seekArcComplete.setProgressColor(Color.parseColor("#22FF22"));
		//seekArcComplete.setProgress(99);

		seekArcWarning.setProgressColor(Color.parseColor("#FF2222"));
		seekArcWarning.setArcColor(Color.parseColor("#c2c2c2"));
		//seekArcWarning.setProgress(33);

	}
	
}
